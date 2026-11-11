var names = {profile: "Профиль", programs: "Программа", workout: "Тренировка", recovery: "Восстановление", nutrition: "Питание", history: "История"};
var muscleNames = {chest: "Грудь", back: "Спина", shoulders: "Плечи", biceps: "Бицепс", triceps: "Трицепс", abs: "Пресс", legs: "Ноги"};
var programs = [];
var templates = [];
var exercises = [];
var selectedExercises = [];
var selectedUserId = localStorage.getItem("potentialmanUserId") || "";
var selectedProgramId = "";
var selectedDayIndex = -1;
var selectedGoal = "maintain";
var currentRecovery = null;
var previewNumber = 0;
var busyCount = 0;
var toastTimer;

function element(id) { return document.getElementById(id); }

function localToday() {
  var today = new Date();
  var month = String(today.getMonth() + 1).padStart(2, "0");
  var day = String(today.getDate()).padStart(2, "0");
  return today.getFullYear() + "-" + month + "-" + day;
}

function escapeText(value) {
  return String(value).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}

function toast(message, error) {
  var box = element("toast");
  box.textContent = message;
  box.className = error ? "toast error" : "toast";
  box.hidden = false;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(function () { box.hidden = true; }, 3500);
}

function request(path, method, body, quiet) {
  if (!quiet) {
    busyCount++;
    element("loading").hidden = false;
  }
  var options = {method: method || "GET"};
  if (body) {
    options.headers = {"Content-Type": "application/json"};
    options.body = JSON.stringify(body);
  }
  return fetch(path, options).then(function (response) {
    return response.text().then(function (text) {
      var data = text ? JSON.parse(text) : {};
      if (!response.ok) throw new Error(data.error || "Ошибка запроса");
      return data;
    });
  }).finally(function () {
    if (!quiet) {
      busyCount--;
      if (busyCount <= 0) element("loading").hidden = true;
    }
  });
}

function showPage(pageId, saveHistory) {
  if (!names[pageId]) pageId = "profile";
  var pages = document.querySelectorAll(".page");
  var buttons = document.querySelectorAll(".nav-item");
  for (var i = 0; i < pages.length; i++) {
    var selected = pages[i].id === pageId;
    pages[i].hidden = !selected;
    pages[i].classList.toggle("active", selected);
  }
  for (var j = 0; j < buttons.length; j++) {
    var current = buttons[j].getAttribute("data-page") === pageId;
    buttons[j].classList.toggle("active", current);
    if (current) buttons[j].setAttribute("aria-current", "page");
    else buttons[j].removeAttribute("aria-current");
  }
  element("breadcrumb-current").textContent = names[pageId];
  document.title = names[pageId] + " — PotentialMan";
  if (location.hash !== "#" + pageId) {
    if (saveHistory) history.pushState(null, "", "#" + pageId);
    else history.replaceState(null, "", "#" + pageId);
  }
  window.scrollTo(0, 0);
  closeMenu();
  if (pageId === "recovery") loadRecovery();
  if (pageId === "nutrition") loadNutrition();
  if (pageId === "history") loadHistory();
  if (pageId === "workout") renderWeeklyPlan();
}

function openMenu() {
  element("sidebar").classList.add("open");
  element("menu-backdrop").hidden = false;
  element("menu-toggle").setAttribute("aria-expanded", "true");
}

function closeMenu() {
  element("sidebar").classList.remove("open");
  element("menu-backdrop").hidden = true;
  element("menu-toggle").setAttribute("aria-expanded", "false");
}

function loadUsers() {
  return request("/users").then(function (users) {
    var picker = element("user-select");
    picker.innerHTML = '<option value="">Новый профиль</option>';
    var found = false;
    for (var i = 0; i < users.length; i++) {
      var option = document.createElement("option");
      option.value = users[i].id;
      option.textContent = "Профиль #" + users[i].id;
      picker.appendChild(option);
      if (String(users[i].id) === String(selectedUserId)) found = true;
    }
    if (!found) {
      selectedUserId = "";
      localStorage.removeItem("potentialmanUserId");
    }
    picker.value = selectedUserId;
    if (selectedUserId) loadProfile();
    else clearProfile();
    selectedProgramId = localStorage.getItem("potentialmanProgramId:" + selectedUserId) || localStorage.getItem("potentialmanProgramId") || "";
    return loadPrograms().then(function () { return loadTemplates(); });
  }).catch(function (error) { toast("Не удалось загрузить пользователей: " + error.message, true); });
}

function clearProfile() {
  element("profile-form").reset();
  element("activity-hint").textContent = "Активность влияет на калории и белок. Можно выбрать уровень вручную или доверить подбору.";
  element("bmi-number").textContent = "—";
  element("bmr-number").textContent = "—";
  element("bmi-category").textContent = "Заполни профиль";
  element("bmi-category").className = "metric-tag";
}

function loadProfile() {
  if (!selectedUserId) { clearProfile(); return; }
  request("/users/" + selectedUserId).then(function (user) {
    element("age").value = user.age;
    element("weight").value = user.weight;
    element("height").value = user.height;
    element("gender").value = user.gender;
    element("steps-per-day").value = user.stepsPerDay;
    element("workouts-per-week").value = user.workoutsPerWeek;
    element("training-intensity").value = user.trainingIntensity;
    element("activity-level").value = user.activityLevel;
    var activityNames = {rest: "почти без движения", light: "лёгкая", moderate: "средняя", high: "высокая"};
    element("activity-hint").textContent = "Для расчёта выбрана активность: " + activityNames[user.effectiveActivity] + " (коэффициент " + user.activityFactor + ").";
    element("bmi-number").textContent = user.bmi;
    element("bmr-number").textContent = user.bmr;
    element("bmi-category").textContent = user.bmiCategory;
    element("bmi-category").className = user.bmiCategory === "Норма" ? "metric-tag" : "metric-tag warn";
  }).catch(function (error) { toast(error.message, true); });
}

function saveProfile(event) {
  event.preventDefault();
  var data = {
    age: Number(element("age").value), weight: Number(element("weight").value),
    height: Number(element("height").value), gender: element("gender").value,
    stepsPerDay: Number(element("steps-per-day").value), workoutsPerWeek: Number(element("workouts-per-week").value),
    trainingIntensity: element("training-intensity").value, activityLevel: element("activity-level").value
  };
  var path = selectedUserId ? "/users/" + selectedUserId : "/users";
  var method = selectedUserId ? "PUT" : "POST";
  request(path, method, data).then(function (user) {
    selectedUserId = String(user.id);
    localStorage.setItem("potentialmanUserId", selectedUserId);
    toast("Профиль сохранён");
    loadUsers();
  }).catch(function (error) { toast(error.message, true); });
}

function renderPrograms() {
  var container = element("program-list");
  var html = "";
  for (var i = 0; i < programs.length; i++) {
    var program = programs[i];
    var active = program.id === selectedProgramId;
    html += '<article class="program-card' + (active ? ' recommended' : '') + '">';
    html += '<span class="overline">' + (program.id.indexOf("custom-") === 0 ? 'МОЯ ПРОГРАММА' : 'ПРОГРАММА ' + (i + 1)) + '</span>';
    if (active) html += '<span class="recommend-badge">✓ Выбрано</span>';
    var dayWord = program.days === 3 || program.days === 4 ? "дня" : "дней";
    html += '<h2>' + escapeText(program.name) + '</h2><span class="program-days">' + program.days + ' ' + dayWord + ' в неделю</span><div class="schedule">';
    for (var j = 0; j < program.schedule.length; j++) html += '<span>' + escapeText(program.schedule[j]) + '</span>';
    html += '</div><div class="program-actions"><button class="secondary-button" type="button" data-program="' + escapeText(program.id) + '">' + (active ? 'Выбрана' : 'Выбрать программу') + '</button>';
    if (program.id.indexOf("custom-") === 0) html += '<button class="secondary-button danger-button" type="button" data-delete-program="' + escapeText(program.id) + '">Удалить</button>';
    html += '</div></article>';
  }
  container.innerHTML = html;
}

function loadPrograms() {
  var path = selectedUserId ? "/programs?userId=" + selectedUserId : "/programs";
  return request(path).then(function (data) {
    programs = data;
    var found = false;
    for (var i = 0; i < programs.length; i++) if (programs[i].id === selectedProgramId) found = true;
    if (!found) { selectedProgramId = ""; selectedDayIndex = -1; }
    renderPrograms();
    if (selectedProgramId && selectedDayIndex < 0) {
      var savedDay = localStorage.getItem("potentialmanDay:" + selectedUserId + ":" + selectedProgramId);
      selectedDayIndex = savedDay === null ? 0 : Number(savedDay);
    }
    renderWeeklyPlan();
  }).catch(function (error) { toast("Не удалось загрузить программы: " + error.message, true); });
}

function chooseProgram(id) {
  selectedProgramId = id;
  selectedDayIndex = 0;
  if (selectedUserId) localStorage.setItem("potentialmanProgramId:" + selectedUserId, id);
  else localStorage.setItem("potentialmanProgramId", id);
  renderPrograms();
  selectDay(0);
}

function recommendProgram() {
  var days = Number(element("training-days").value);
  if (days < 1 || days > 7) { toast("Введите число дней от 1 до 7", true); return; }
  request("/programs/split?days=" + days).then(function (data) {
    chooseProgram(data.recommendedId);
    for (var i = 0; i < programs.length; i++) {
      if (programs[i].id === selectedProgramId) element("recommend-text").textContent = "Рекомендуем: " + programs[i].name;
    }
  }).catch(function (error) { toast(error.message, true); });
}

function currentProgram() {
  for (var i = 0; i < programs.length; i++) if (programs[i].id === selectedProgramId) return programs[i];
  return null;
}

function templateForDay(programId, dayIndex) {
  for (var i = 0; i < templates.length; i++) {
    if (templates[i].programId === programId && templates[i].dayIndex === dayIndex) return templates[i];
  }
  return null;
}

function loadTemplates() {
  if (!selectedUserId) {
    templates = [];
    renderWeeklyPlan();
    return Promise.resolve();
  }
  return request("/users/" + selectedUserId + "/templates").then(function (data) {
    templates = data;
    var saved = templateForDay(selectedProgramId, selectedDayIndex);
    if (saved) loadTemplate(saved);
    else if (currentProgram() && selectedDayIndex >= 0) selectDay(selectedDayIndex);
    else renderWeeklyPlan();
  }).catch(function (error) { toast("Не удалось загрузить шаблоны: " + error.message, true); });
}

function loadTemplate(template) {
  selectedExercises = [];
  for (var i = 0; i < template.exercises.length; i++) {
    var item = template.exercises[i];
    selectedExercises.push({id: item.id, name: item.name, group: item.group, sets: item.sets, reps: item.reps,
      dropSet: item.dropSet, superSet: item.superSet, heavyWeight: item.heavyWeight});
  }
  element("workout-day").value = template.name;
  renderSelected();
  previewWorkout();
}

function renderWeeklyPlan() {
  var program = currentProgram();
  var html = "";
  element("weekly-title").textContent = program ? program.name : "Выбери программу";
  if (!selectedUserId) element("weekly-hint").textContent = "Создай профиль, чтобы сохранять шаблоны дней. Программу можно посмотреть уже сейчас.";
  else if (!program) element("weekly-hint").textContent = "Выбери готовую программу или создай свою на вкладке «Программа».";
  else element("weekly-hint").textContent = "Нажми на день: сохранённый шаблон загрузится. Для пустого дня можно сохранить текущий список упражнений.";
  if (program) {
    for (var i = 0; i < program.schedule.length; i++) {
      var saved = templateForDay(program.id, i);
      html += '<button class="weekly-day' + (selectedDayIndex === i ? ' active' : '') + (saved ? ' saved' : '') + '" type="button" data-day-index="' + i + '"><strong>' + escapeText(program.schedule[i]) + '</strong><small>' + (saved ? 'Шаблон сохранён' : 'Нет шаблона') + '</small></button>';
    }
  }
  element("weekly-days").innerHTML = html;
  element("save-template").disabled = !selectedUserId || !program || selectedDayIndex < 0;
  element("delete-template").hidden = !program || !templateForDay(program.id, selectedDayIndex);
  element("delete-template").removeAttribute("data-armed");
  element("delete-template").textContent = "Удалить шаблон дня";
}

function selectDay(index) {
  var program = currentProgram();
  if (!program || index < 0 || index >= program.schedule.length) return;
  selectedDayIndex = index;
  if (selectedUserId) localStorage.setItem("potentialmanDay:" + selectedUserId + ":" + selectedProgramId, String(index));
  var saved = templateForDay(program.id, index);
  if (saved) loadTemplate(saved);
  else {
    var parts = program.schedule[index].split("·");
    element("workout-day").value = parts.length > 1 ? parts[1].trim() : program.name;
  }
  renderWeeklyPlan();
}

function saveTemplate() {
  if (!selectedUserId || !currentProgram() || selectedDayIndex < 0) { toast("Выбери профиль, программу и день", true); return; }
  if (selectedExercises.length === 0) { toast("Добавь хотя бы одно упражнение", true); return; }
  var data = buildPayload();
  data.programId = selectedProgramId;
  data.dayIndex = selectedDayIndex;
  data.name = data.day;
  request("/users/" + selectedUserId + "/templates", "PUT", data).then(function () {
    return loadTemplates();
  }).then(function () { toast("Шаблон дня сохранён. В следующий раз он загрузится сам."); })
    .catch(function (error) { toast(error.message, true); });
}

function deleteTemplate() {
  var button = element("delete-template");
  if (!button.hasAttribute("data-armed")) {
    button.setAttribute("data-armed", "true");
    button.textContent = "Нажми ещё раз для удаления";
    return;
  }
  request("/users/" + selectedUserId + "/templates/" + selectedProgramId + "/" + selectedDayIndex, "DELETE")
    .then(function () { return loadTemplates(); })
    .then(function () { toast("Шаблон удалён"); })
    .catch(function (error) { toast(error.message, true); });
}

function saveCustomProgram(event) {
  event.preventDefault();
  if (!selectedUserId) { toast("Сначала создай профиль", true); showPage("profile", true); return; }
  var days = [];
  var checkboxes = document.querySelectorAll("[data-weekday]");
  for (var i = 0; i < checkboxes.length; i++) {
    if (checkboxes[i].checked) {
      var weekday = Number(checkboxes[i].getAttribute("data-weekday"));
      var name = document.querySelector('[data-day-name="' + weekday + '"]').value.trim();
      if (!name) { toast("Назови тренировку для каждого выбранного дня", true); return; }
      days.push({weekday: weekday, label: name});
    }
  }
  if (days.length === 0) { toast("Выбери хотя бы один день", true); return; }
  var data = {userId: Number(selectedUserId), name: element("custom-program-name").value.trim(), days: days};
  request("/programs/custom", "POST", data).then(function (created) {
    element("custom-program-form").reset();
    for (var i = 0; i < checkboxes.length; i++) document.querySelector('[data-day-name="' + checkboxes[i].getAttribute("data-weekday") + '"]').disabled = true;
    element("custom-program-panel").open = false;
    return loadPrograms().then(function () { chooseProgram(created.id); });
  }).then(function () { toast("Своя программа сохранена. Назначь тренировки на её дни."); })
    .catch(function (error) { toast(error.message, true); });
}

function deleteCustomProgram(button) {
  if (!button.hasAttribute("data-armed")) {
    button.setAttribute("data-armed", "true");
    button.textContent = "Нажми ещё раз";
    return;
  }
  var id = button.getAttribute("data-delete-program");
  request("/programs/custom/" + id + "?userId=" + selectedUserId, "DELETE").then(function () {
    if (selectedProgramId === id) {
      selectedProgramId = "";
      selectedDayIndex = -1;
      localStorage.removeItem("potentialmanProgramId:" + selectedUserId);
    }
    return loadPrograms().then(function () { return loadTemplates(); });
  }).then(function () { toast("Своя программа удалена"); })
    .catch(function (error) { toast(error.message, true); });
}

function loadExercises() {
  request("/exercises").then(function (data) { exercises = data; renderCatalog(); })
    .catch(function (error) { toast("Не удалось загрузить упражнения: " + error.message, true); });
}

function renderCatalog() {
  var search = element("exercise-search").value.toLowerCase();
  var group = element("muscle-filter").value;
  var html = "";
  for (var i = 0; i < exercises.length; i++) {
    var exercise = exercises[i];
    if (group && exercise.group !== group) continue;
    if (search && exercise.name.toLowerCase().indexOf(search) === -1) continue;
    html += '<div class="catalog-item"><div><strong>' + escapeText(exercise.name) + '</strong><small>' + escapeText(exercise.group) + '</small></div><button class="icon-button" type="button" data-add="' + escapeText(exercise.id) + '" aria-label="Добавить ' + escapeText(exercise.name) + '">+</button></div>';
  }
  element("exercise-list").innerHTML = html || '<p class="empty-message">Ничего не найдено.</p>';
}

function addExercise(id) {
  for (var i = 0; i < exercises.length; i++) {
    if (exercises[i].id === id) {
      selectedExercises.push({id: id, name: exercises[i].name, group: exercises[i].group, sets: 3, reps: 10, dropSet: false, superSet: false, heavyWeight: false});
      renderSelected();
      previewWorkout();
      return;
    }
  }
}

function renderSelected() {
  element("exercise-count").textContent = selectedExercises.length;
  if (selectedExercises.length === 0) {
    element("selected-exercises").innerHTML = '<p class="empty-message">Добавь упражнение из каталога слева.</p>';
    return;
  }
  var html = "";
  for (var i = 0; i < selectedExercises.length; i++) {
    var item = selectedExercises[i];
    html += '<div class="selected-item" data-index="' + i + '"><div class="selected-top"><div><strong>' + escapeText(item.name) + '</strong><small>' + escapeText(item.group) + '</small></div><button class="icon-button delete" type="button" data-remove="' + i + '" aria-label="Удалить упражнение">×</button></div>';
    html += '<div class="number-fields"><label>Подходы<input type="number" min="1" max="10" value="' + item.sets + '" data-field="sets"></label><label>Повторения<input type="number" min="1" max="50" value="' + item.reps + '" data-field="reps"></label></div>';
    html += '<div class="modifiers"><label><input type="checkbox" data-field="dropSet"' + (item.dropSet ? ' checked' : '') + '> Дропсет</label><label><input type="checkbox" data-field="superSet"' + (item.superSet ? ' checked' : '') + '> Суперсет</label><label><input type="checkbox" data-field="heavyWeight"' + (item.heavyWeight ? ' checked' : '') + '> Тяжёлый вес</label></div></div>';
  }
  element("selected-exercises").innerHTML = html;
}

function handleSelectedChange(event) {
  var field = event.target.getAttribute("data-field");
  if (!field) return;
  var card = event.target.closest(".selected-item");
  var item = selectedExercises[Number(card.getAttribute("data-index"))];
  if (field === "sets" || field === "reps") item[field] = Number(event.target.value);
  else item[field] = event.target.checked;
  previewWorkout();
}

function buildPayload() {
  var items = [];
  for (var i = 0; i < selectedExercises.length; i++) {
    var item = selectedExercises[i];
    items.push({id: item.id, sets: item.sets, reps: item.reps, dropSet: item.dropSet, superSet: item.superSet, heavyWeight: item.heavyWeight});
  }
  return {userId: Number(selectedUserId), programId: selectedProgramId, dayIndex: selectedDayIndex,
    day: element("workout-day").value.trim() || "Моя тренировка", trainingDate: element("training-date").value, exercises: items};
}

function loadColor(level) {
  if (level >= 8) return "#f27465";
  if (level >= 5) return "#ffbd73";
  return "#bbf25a";
}

function renderLoad(load) {
  var html = "";
  for (var muscle in muscleNames) {
    var value = load[muscle] || 0;
    html += '<div class="load-row"><div class="load-label"><span>' + muscleNames[muscle] + '</span><strong>' + value + '/10</strong></div><div class="load-track"><div class="load-fill" style="width:' + (value * 10) + '%;background:' + loadColor(value) + '"></div></div></div>';
  }
  element("load-bars").innerHTML = html;
  var shapes = document.querySelectorAll(".body-map [data-muscle]");
  for (var i = 0; i < shapes.length; i++) {
    var name = shapes[i].getAttribute("data-muscle");
    shapes[i].style.fill = load[name] ? loadColor(load[name]) : "#344238";
  }
  var warnings = [];
  if (currentRecovery && currentRecovery.muscles) {
    for (var j = 0; j < currentRecovery.muscles.length; j++) {
      var item = currentRecovery.muscles[j];
      if (!item.ready && load[item.id] > 0) warnings.push(item.name);
    }
  }
  element("workout-warning").hidden = warnings.length === 0;
  element("workout-warning").textContent = warnings.length ? "Ещё восстанавливаются: " + warnings.join(", ") : "";
}

function previewWorkout() {
  var number = ++previewNumber;
  var payload = buildPayload();
  for (var i = 0; i < payload.exercises.length; i++) {
    if (payload.exercises[i].sets < 1 || payload.exercises[i].reps < 1) return;
  }
  request("/workouts/build", "POST", payload, true).then(function (data) {
    if (number !== previewNumber) return;
    renderLoad(data.load);
    element("workout-calories").textContent = data.calories;
  }).catch(function (error) { if (number === previewNumber) toast(error.message, true); });
}

function saveWorkout() {
  if (!selectedUserId) { toast("Сначала создай профиль", true); showPage("profile", true); return; }
  if (selectedExercises.length === 0) { toast("Добавь хотя бы одно упражнение", true); return; }
  if (!element("training-date").value || element("training-date").value > localToday()) { toast("Выбери прошедшую или сегодняшнюю дату", true); return; }
  request("/workouts/built", "POST", buildPayload()).then(function () {
    element("training-date").value = localToday();
    loadRecovery();
    toast("Тренировка записана в историю. Список упражнений остался в конструкторе.");
  }).catch(function (error) { toast(error.message, true); });
}

function loadRecovery() {
  if (!selectedUserId) { element("recovery-list").innerHTML = '<p class="empty-message">Сначала создай профиль.</p>'; currentRecovery = null; return; }
  request("/users/" + selectedUserId + "/recovery").then(function (data) {
    currentRecovery = data;
    var html = "";
    for (var i = 0; i < data.muscles.length; i++) {
      var item = data.muscles[i];
      html += '<article class="recovery-item"><header><strong>' + escapeText(item.name) + '</strong><span class="' + (item.ready ? 'ready' : 'not-ready') + '">' + (item.ready ? 'Готова' : 'Отдыхает') + '</span></header><small>' + (item.ready ? 'Можно тренировать' : 'Готова через ' + item.remainingHours + ' ч') + '</small><div class="recovery-track"><div class="recovery-fill" style="width:' + item.progress + '%;background:' + (item.ready ? '#bbf25a' : '#ffb45c') + '"></div></div></article>';
    }
    element("recovery-list").innerHTML = html;
    previewWorkout();
  }).catch(function (error) { toast(error.message, true); });
}

function loadNutrition() {
  if (!selectedUserId) { element("nutrition-calories").textContent = "—"; return; }
  request("/users/" + selectedUserId + "/nutrition?goal=" + selectedGoal).then(function (data) {
    element("nutrition-calories").textContent = data.calories;
    element("burned-today").textContent = data.burnedToday + " ккал";
    element("remaining-calories").textContent = data.remaining + " ккал";
    element("protein").textContent = data.protein + " г";
    var activityNames = {rest: "почти без движения", light: "лёгкая", moderate: "средняя", high: "высокая"};
    element("activity-description").textContent = "Активность: " + activityNames[data.activityLevel] + ".";
    element("protein-explanation").textContent = "Белок: " + data.proteinPerKg + " г/кг веса по выбранной активности и тяжести тренировок.";
    element("fat").textContent = data.fat + " г";
    element("carbs").textContent = data.carbs + " г";
    var total = data.protein * 4 + data.fat * 9 + data.carbs * 4;
    var proteinEnd = data.protein * 400 / total;
    var fatEnd = proteinEnd + data.fat * 900 / total;
    element("macro-donut").style.background = "conic-gradient(#bbf25a 0 " + proteinEnd + "%, #ffbd73 " + proteinEnd + "% " + fatEnd + "%, #728fef " + fatEnd + "% 100%)";
  }).catch(function (error) { toast(error.message, true); });
}

function loadHistory() {
  if (!selectedUserId) { element("history-list").innerHTML = '<p class="empty-message">Сначала создай профиль.</p>'; return; }
  request("/users/" + selectedUserId + "/history").then(function (data) {
    if (data.length === 0) { element("history-list").innerHTML = '<p class="empty-message">Здесь появится первая сохранённая тренировка.</p>'; return; }
    data.sort(function (a, b) { return String(b.date).localeCompare(String(a.date)); });
    var html = "";
    var currentDate = "";
    for (var i = 0; i < data.length; i++) {
      var workout = data[i];
      var dateKey = String(workout.date).substring(0, 10);
      if (dateKey !== currentDate) {
        if (currentDate) html += '</div></section>';
        currentDate = dateKey;
        var heading = new Date(dateKey + "T12:00:00").toLocaleDateString("ru-RU", {weekday: "long", day: "numeric", month: "long", year: "numeric"});
        html += '<section class="history-day"><h2>' + escapeText(heading.charAt(0).toUpperCase() + heading.substring(1)) + '</h2><div class="history-day-list">';
      }
      var groups = [];
      for (var muscle in muscleNames) if (workout.load[muscle] > 0) groups.push(muscleNames[muscle]);
      var time = new Date(workout.date).toLocaleTimeString("ru-RU", {hour: "2-digit", minute: "2-digit"});
      html += '<article class="history-card"><div class="history-item"><div><span class="overline">' + escapeText(time) + '</span><h3>' + escapeText(workout.day) + '</h3><p>' + escapeText(groups.join(" · ")) + ' · ' + workout.exercises.length + ' упр.</p></div><div class="history-right"><span class="history-calories">' + workout.calories + ' ккал</span><button class="secondary-button history-toggle" type="button" data-toggle="' + workout.id + '" aria-expanded="false">Упражнения ↓</button><button class="icon-button delete" type="button" data-delete="' + workout.id + '" aria-label="Удалить тренировку">×</button></div></div><div class="history-details" id="history-details-' + workout.id + '" hidden>';
      for (var j = 0; j < workout.exercises.length; j++) {
        var exercise = workout.exercises[j];
        var modifiers = [];
        if (exercise.dropSet) modifiers.push("дропсет");
        if (exercise.superSet) modifiers.push("суперсет");
        if (exercise.heavyWeight) modifiers.push("тяжёлый вес");
        html += '<div class="history-exercise"><strong>' + escapeText(exercise.name) + '</strong><span>' + exercise.sets + ' × ' + exercise.reps + '</span><small>' + escapeText(modifiers.join(" · ")) + '</small></div>';
      }
      html += '</div></article>';
    }
    html += '</div></section>';
    element("history-list").innerHTML = html;
  }).catch(function (error) { toast(error.message, true); });
}

var navButtons = document.querySelectorAll(".nav-item");
for (var i = 0; i < navButtons.length; i++) {
  navButtons[i].addEventListener("click", function () { showPage(this.getAttribute("data-page"), true); });
}
element("menu-toggle").addEventListener("click", function () { if (element("sidebar").classList.contains("open")) closeMenu(); else openMenu(); });
element("menu-backdrop").addEventListener("click", closeMenu);
document.addEventListener("keydown", function (event) { if (event.key === "Escape") closeMenu(); });
window.addEventListener("popstate", function () { showPage(location.hash.substring(1), false); });
element("user-select").addEventListener("change", function () {
  selectedUserId = this.value;
  if (selectedUserId) localStorage.setItem("potentialmanUserId", selectedUserId);
  else localStorage.removeItem("potentialmanUserId");
  loadProfile();
  currentRecovery = null;
  templates = [];
  selectedDayIndex = -1;
  selectedExercises = [];
  renderSelected();
  previewWorkout();
  selectedProgramId = localStorage.getItem("potentialmanProgramId:" + selectedUserId) || localStorage.getItem("potentialmanProgramId") || "";
  loadPrograms().then(function () { return loadTemplates(); });
  loadRecovery();
  loadNutrition();
  loadHistory();
});
element("profile-form").addEventListener("submit", saveProfile);
element("recommend-button").addEventListener("click", recommendProgram);
element("custom-program-form").addEventListener("submit", saveCustomProgram);
element("custom-days").addEventListener("change", function (event) {
  var checkbox = event.target.closest("[data-weekday]");
  if (!checkbox) return;
  var name = document.querySelector('[data-day-name="' + checkbox.getAttribute("data-weekday") + '"]');
  name.disabled = !checkbox.checked;
  name.required = checkbox.checked;
});
element("program-list").addEventListener("click", function (event) {
  var deleteButton = event.target.closest("[data-delete-program]");
  if (deleteButton) { deleteCustomProgram(deleteButton); return; }
  var button = event.target.closest("[data-program]");
  if (!button) return;
  chooseProgram(button.getAttribute("data-program"));
  toast("Программа выбрана");
});
element("open-programs").addEventListener("click", function () { showPage("programs", true); });
element("weekly-days").addEventListener("click", function (event) {
  var button = event.target.closest("[data-day-index]");
  if (button) selectDay(Number(button.getAttribute("data-day-index")));
});
element("save-template").addEventListener("click", saveTemplate);
element("delete-template").addEventListener("click", deleteTemplate);
element("clear-builder").addEventListener("click", function () {
  selectedExercises = [];
  renderSelected();
  previewWorkout();
  toast("Список упражнений очищен. Сохранённый шаблон дня не изменился.");
});
element("exercise-search").addEventListener("input", renderCatalog);
element("muscle-filter").addEventListener("change", renderCatalog);
element("exercise-list").addEventListener("click", function (event) {
  var button = event.target.closest("[data-add]");
  if (button) addExercise(button.getAttribute("data-add"));
});
element("selected-exercises").addEventListener("click", function (event) {
  var button = event.target.closest("[data-remove]");
  if (!button) return;
  selectedExercises.splice(Number(button.getAttribute("data-remove")), 1);
  renderSelected();
  previewWorkout();
});
element("selected-exercises").addEventListener("change", handleSelectedChange);
element("selected-exercises").addEventListener("input", function (event) {
  if (event.target.type === "number") handleSelectedChange(event);
});
element("save-workout").addEventListener("click", saveWorkout);
var goalButtons = document.querySelectorAll(".goal-button");
for (var j = 0; j < goalButtons.length; j++) {
  goalButtons[j].addEventListener("click", function () {
    selectedGoal = this.getAttribute("data-goal");
    for (var k = 0; k < goalButtons.length; k++) goalButtons[k].classList.toggle("active", goalButtons[k] === this);
    loadNutrition();
  });
}
element("history-list").addEventListener("click", function (event) {
  var toggle = event.target.closest("[data-toggle]");
  if (toggle) {
    var details = element("history-details-" + toggle.getAttribute("data-toggle"));
    details.hidden = !details.hidden;
    toggle.setAttribute("aria-expanded", details.hidden ? "false" : "true");
    toggle.textContent = details.hidden ? "Упражнения ↓" : "Скрыть ↑";
    return;
  }
  var button = event.target.closest("[data-delete]");
  if (!button) return;
  request("/workouts/built/" + button.getAttribute("data-delete"), "DELETE").then(function () {
    loadHistory();
    loadRecovery();
    loadNutrition();
    toast("Тренировка удалена");
  }).catch(function (error) { toast(error.message, true); });
});

renderLoad({});
element("training-date").value = localToday();
element("training-date").max = localToday();
showPage(location.hash.substring(1), false);
loadUsers();
loadExercises();

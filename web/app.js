var names = {profile: "Профиль", programs: "Программа", workout: "Тренировка", recovery: "Восстановление", nutrition: "Питание", history: "История", progress: "Прогресс"};
var muscleNames = {chest: "Грудь", back: "Спина", shoulders: "Плечи", biceps: "Бицепс", triceps: "Трицепс", abs: "Пресс", legs: "Ноги"};
var programs = [];
var templates = [];
var journalEntries = [];
var historyData = [];
var strengthData = {records: [], goals: []};
var bodyWeightData = [];
var timerState = {index: 0, set: 1, phase: "idle", startedAt: 0, restUntil: 0, lastSeconds: 0};
var strengthEditId = 0;
var nutritionTarget = 0;
var favoritesOnly = false;
var favorites = (localStorage.getItem("potentialmanFavorites") || "").split(",");
var exercises = [];
var selectedExercises = [];
var selectedUserId = localStorage.getItem("potentialmanUserId") || "";
var restOverrides = readRestOverrides();
var timerAudio = null;
var alarmInterval = null;
var alarmTimeout = null;
var alarmTone = null;
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
  if (pageId === "progress") { loadStrength(); loadBodyWeights(); }
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
    return loadPrograms().then(function () { return loadTemplates(); }).then(function () { return loadJournal(); });
  }).then(function () {
    if (selectedUserId) { loadRecovery(); loadNutrition(); loadHistory(); loadStrength(); loadBodyWeights(); }
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
  request("/programs/split?days=" + days + "&goal=" + element("program-goal").value).then(function (data) {
    chooseProgram(data.recommendedId);
    for (var i = 0; i < programs.length; i++) {
      if (programs[i].id === selectedProgramId) element("recommend-text").textContent = "Рекомендуем: " + programs[i].name + " (" + programs[i].days + " дня/дней). Для другого расписания создай свою программу.";
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
      dropSet: item.dropSet, superSet: item.superSet, heavyWeight: item.heavyWeight, unit: item.unit || "reps", weightKg: item.weightKg || 0, setSeconds: []});
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
  element("load-starter").hidden = !program || !program.starter || !program.starter[selectedDayIndex]
    || !!templateForDay(program.id, selectedDayIndex);
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

function loadStarter() {
  var program = currentProgram();
  if (!program || !program.starter || !program.starter[selectedDayIndex]) return;
  var starter = program.starter[selectedDayIndex];
  var list = [];
  for (var i = 0; i < starter.length; i++) {
    var found = null;
    for (var j = 0; j < exercises.length; j++) if (exercises[j].id === starter[i].id) found = exercises[j];
    if (found) list.push({id: found.id, name: found.name, group: found.group, unit: found.unit,
      sets: starter[i].sets, reps: starter[i].reps, dropSet: false, superSet: false, heavyWeight: false, weightKg: 0, setSeconds: []});
  }
  if (list.length === 0) { toast("Каталог ещё загружается", true); return; }
  selectedExercises = list;
  renderSelected();
  previewWorkout();
  toast("Пример загружен. Измени его под себя и сохрани для этого дня.");
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
  request("/exercises").then(function (data) { exercises = data; renderCatalog(); renderStrengthChoices(); renderWorkoutTimer(); })
    .catch(function (error) { toast("Не удалось загрузить упражнения: " + error.message, true); });
}

function renderCatalog() {
  var search = element("exercise-search").value.toLowerCase().replace(/ё/g, "е").trim();
  var group = element("muscle-filter").value;
  var html = "";
  var count = 0;
  for (var i = 0; i < exercises.length; i++) {
    var exercise = exercises[i];
    if (group && exercise.group !== group) continue;
    var favorite = favorites.indexOf(exercise.id) !== -1;
    if (favoritesOnly && !favorite) continue;
    var words = search.split(/\s+/);
    var lookup = (exercise.name + " " + exercise.group + " " + exercise.id.replace(/_/g, " ")).toLowerCase().replace(/ё/g, "е");
    var matches = true;
    for (var j = 0; j < words.length; j++) if (words[j] && lookup.indexOf(words[j]) === -1) matches = false;
    if (!matches) continue;
    count++;
    html += '<div class="catalog-item"><div><strong>' + escapeText(exercise.name) + '</strong><small>' + escapeText(exercise.group) + (exercise.unit === "minutes" ? ' · минуты' : '') + '</small></div><div class="catalog-buttons"><button class="favorite-button' + (favorite ? ' active' : '') + '" type="button" data-favorite="' + escapeText(exercise.id) + '" aria-label="Избранное">' + (favorite ? '★' : '☆') + '</button><button class="icon-button" type="button" data-add="' + escapeText(exercise.id) + '" aria-label="Добавить ' + escapeText(exercise.name) + '">+</button></div></div>';
  }
  element("catalog-count").textContent = "Найдено: " + count;
  element("favorites-only").classList.toggle("active", favoritesOnly);
  element("exercise-list").innerHTML = html || '<p class="empty-message">Ничего не найдено. Попробуй другое слово или сбрось фильтр.</p>';
}

function toggleFavorite(id) {
  var index = favorites.indexOf(id);
  if (index === -1) favorites.push(id);
  else favorites.splice(index, 1);
  localStorage.setItem("potentialmanFavorites", favorites.join(","));
  renderCatalog();
}

function addExercise(id) {
  for (var i = 0; i < exercises.length; i++) {
    if (exercises[i].id === id) {
      var timed = exercises[i].unit === "minutes";
      selectedExercises.push({id: id, name: exercises[i].name, group: exercises[i].group, unit: exercises[i].unit,
        sets: timed ? 1 : 3, reps: timed ? 20 : 10, dropSet: false, superSet: false, heavyWeight: false, weightKg: 0, setSeconds: []});
      renderSelected();
      previewWorkout();
      return;
    }
  }
}

function renderSelected() {
  resetWorkoutTimer();
  element("exercise-count").textContent = selectedExercises.length;
  if (selectedExercises.length === 0) {
    element("selected-exercises").innerHTML = '<p class="empty-message">Добавь упражнение из каталога слева.</p>';
    renderWorkoutTimer();
    return;
  }
  var html = "";
  for (var i = 0; i < selectedExercises.length; i++) {
    var item = selectedExercises[i];
    html += '<div class="selected-item" data-index="' + i + '"><div class="selected-top"><div><strong>' + escapeText(item.name) + '</strong><small>' + escapeText(item.group) + '</small></div><button class="icon-button delete" type="button" data-remove="' + i + '" aria-label="Удалить упражнение">×</button></div>';
    if (item.unit === "minutes") html += '<div class="number-fields"><label>Минуты<input type="number" min="1" max="180" value="' + item.reps + '" data-field="reps"></label></div></div>';
    else {
      html += '<div class="number-fields"><label>Подходы<input type="number" min="1" max="10" value="' + item.sets + '" data-field="sets"></label><label>Повторения<input type="number" min="1" max="50" value="' + item.reps + '" data-field="reps"></label><label>Вес, кг<input type="number" min="0" max="500" step="0.5" value="' + (item.weightKg || "") + '" placeholder="Необязательно" data-field="weightKg"></label></div>';
      html += '<div class="modifiers"><label><input type="checkbox" data-field="dropSet"' + (item.dropSet ? ' checked' : '') + '> Дропсет</label><label><input type="checkbox" data-field="superSet"' + (item.superSet ? ' checked' : '') + '> Суперсет</label><label><input type="checkbox" data-field="heavyWeight"' + (item.heavyWeight ? ' checked' : '') + '> Тяжёлый вес</label></div></div>';
    }
  }
  element("selected-exercises").innerHTML = html;
  renderWorkoutTimer();
}

function clockText(seconds) {
  var minutes = Math.floor(seconds / 60);
  var rest = seconds % 60;
  return String(minutes).padStart(2, "0") + ":" + String(rest).padStart(2, "0");
}

function readRestOverrides() {
  try {
    var saved = JSON.parse(localStorage.getItem("potentialmanRest:" + selectedUserId) || "{}");
    return saved && typeof saved === "object" && !Array.isArray(saved) ? saved : {};
  }
  catch (error) { return {}; }
}

function restSeconds(item) {
  if (restOverrides[item.id]) return restOverrides[item.id];
  if (item.heavyWeight) return 180;
  for (var i = 0; i < exercises.length; i++) if (exercises[i].id === item.id) return exercises[i].restSeconds || (item.unit === "minutes" ? 60 : 75);
  return item.unit === "minutes" ? 60 : 75;
}

function renderWorkoutTimer() {
  var item = selectedExercises[timerState.index];
  var cards = document.querySelectorAll(".selected-item");
  for (var c = 0; c < cards.length; c++) cards[c].classList.toggle("timer-active", c === timerState.index);
  var finished = !item && selectedExercises.length > 0;
  element("timer-exercise").textContent = item ? item.name : finished ? "Все упражнения закончены" : "Добавь упражнение в конструктор.";
  element("timer-set").textContent = item ? "Подход " + timerState.set + " из " + item.sets : "Подход —";
  element("timer-phase").textContent = timerState.phase === "work" ? "Время подхода" : timerState.phase === "rest" ? "Отдых" : finished ? "Готово" : "Ожидание";
  element("timer-rest-hint").textContent = item ? "Отдых после подхода: " + clockText(restSeconds(item)) + ". После последнего подхода откроется следующее упражнение." : "После завершения подхода начнётся отдых. Время зависит от упражнения.";
  element("timer-rest-custom").disabled = !item;
  element("timer-rest-custom").value = item && restOverrides[item.id] ? restOverrides[item.id] : "";
  element("timer-rest-auto").disabled = !item;
  element("timer-start").hidden = !item || timerState.phase !== "idle";
  element("timer-stop").hidden = timerState.phase !== "work";
  element("timer-skip").hidden = timerState.phase !== "rest";
  if (timerState.phase === "idle") element("timer-clock").textContent = clockText(timerState.lastSeconds);
  element("timer-rest-clock").textContent = timerState.phase === "rest" ? clockText(Math.max(0, Math.ceil((timerState.restUntil - Date.now()) / 1000))) : "00:00";
  var results = "";
  for (var i = 0; i < selectedExercises.length; i++) {
    var times = selectedExercises[i].setSeconds || [];
    if (times.length === 0) continue;
    var values = [];
    for (var j = 0; j < times.length; j++) if (times[j]) values.push(clockText(times[j]));
    if (values.length) results += '<div><strong>' + escapeText(selectedExercises[i].name) + '</strong><span>' + values.join(" · ") + '</span></div>';
  }
  element("timer-results").innerHTML = results;
}

function resetWorkoutTimer() {
  stopTimerSound();
  timerState = {index: 0, set: 1, phase: "idle", startedAt: 0, restUntil: 0, lastSeconds: 0};
  element("timer-clock").textContent = "00:00";
  renderWorkoutTimer();
}

function clearWorkoutTimer() {
  for (var i = 0; i < selectedExercises.length; i++) selectedExercises[i].setSeconds = [];
  resetWorkoutTimer();
}

function startWorkoutSet() {
  if (!selectedExercises[timerState.index] || timerState.phase !== "idle") return;
  stopTimerSound();
  prepareTimerAudio();
  timerState.phase = "work";
  timerState.startedAt = Date.now();
  timerState.lastSeconds = 0;
  element("timer-clock").textContent = "00:00";
  renderWorkoutTimer();
}

function stopWorkoutSet() {
  if (timerState.phase !== "work") return;
  var item = selectedExercises[timerState.index];
  timerState.lastSeconds = Math.max(1, Math.ceil((Date.now() - timerState.startedAt) / 1000));
  if (!item.setSeconds) item.setSeconds = [];
  item.setSeconds[timerState.set - 1] = timerState.lastSeconds;
  if (timerState.set >= item.sets) {
    timerState.index++;
    timerState.set = 1;
    timerState.phase = "idle";
    timerState.lastSeconds = 0;
    element("timer-clock").textContent = "00:00";
    toast(selectedExercises[timerState.index] ? "Следующее упражнение: " + selectedExercises[timerState.index].name : "Все упражнения завершены");
  } else {
    timerState.set++;
    timerState.phase = "rest";
    timerState.restUntil = Date.now() + restSeconds(item) * 1000;
  }
  renderWorkoutTimer();
}

function skipWorkoutRest() {
  if (timerState.phase !== "rest") return;
  timerState.phase = "idle";
  timerState.lastSeconds = 0;
  renderWorkoutTimer();
}

function tickWorkoutTimer() {
  if (timerState.phase === "work") element("timer-clock").textContent = clockText(Math.floor((Date.now() - timerState.startedAt) / 1000));
  if (timerState.phase === "rest") {
    var remaining = Math.max(0, Math.ceil((timerState.restUntil - Date.now()) / 1000));
    element("timer-rest-clock").textContent = clockText(remaining);
    if (remaining === 0) { skipWorkoutRest(); playTimerSound(); toast("Отдых закончен. Начни следующий подход."); }
  }
}

function prepareTimerAudio() {
  if (!element("timer-sound").checked || !(window.AudioContext || window.webkitAudioContext)) return;
  try {
    if (!timerAudio) timerAudio = new (window.AudioContext || window.webkitAudioContext)();
    if (timerAudio.state === "suspended") timerAudio.resume().catch(function () {});
  } catch (error) { timerAudio = null; }
}

function stopTimerSound() {
  clearInterval(alarmInterval);
  clearTimeout(alarmTimeout);
  alarmInterval = null;
  alarmTimeout = null;
  if (alarmTone) {
    try { alarmTone.stop(); } catch (error) {}
    alarmTone = null;
  }
}

function playTimerBeep() {
  try {
    var tone = timerAudio.createOscillator();
    var volume = timerAudio.createGain();
    tone.frequency.value = 880;
    volume.gain.value = 0.08;
    tone.connect(volume);
    volume.connect(timerAudio.destination);
    alarmTone = tone;
    tone.onended = function () { if (alarmTone === tone) alarmTone = null; };
    tone.start();
    tone.stop(timerAudio.currentTime + 0.2);
    return true;
  } catch (error) { return false; }
}

function playTimerSound() {
  stopTimerSound();
  if (!element("timer-sound").checked || !timerAudio) return;
  if (!playTimerBeep()) return;
  alarmInterval = setInterval(function () { if (!playTimerBeep()) stopTimerSound(); }, 500);
  alarmTimeout = setTimeout(stopTimerSound, 5500);
}

function saveCustomRest() {
  var item = selectedExercises[timerState.index];
  if (!item) return;
  var input = element("timer-rest-custom");
  if (input.value && !input.checkValidity()) { toast("Отдых: от 15 до 600 секунд, шаг 15", true); return; }
  if (input.value) restOverrides[item.id] = Number(input.value);
  else delete restOverrides[item.id];
  localStorage.setItem("potentialmanRest:" + selectedUserId, JSON.stringify(restOverrides));
  renderWorkoutTimer();
  toast(input.value ? "Время отдыха сохранено для следующих подходов" : "Автоматическое время включено для следующих подходов");
}

function handleSelectedChange(event) {
  var field = event.target.getAttribute("data-field");
  if (!field) return;
  var card = event.target.closest(".selected-item");
  var item = selectedExercises[Number(card.getAttribute("data-index"))];
  if (field === "sets" || field === "reps" || field === "weightKg") item[field] = Number(event.target.value);
  else item[field] = event.target.checked;
  if (field === "sets" && item.setSeconds) item.setSeconds.length = Math.min(item.setSeconds.length, item.sets);
  previewWorkout();
  renderWorkoutTimer();
}

function buildPayload() {
  var items = [];
  for (var i = 0; i < selectedExercises.length; i++) {
    var item = selectedExercises[i];
    items.push({id: item.id, sets: item.sets, reps: item.reps, weightKg: item.weightKg || 0, setSeconds: item.setSeconds || [],
      dropSet: item.dropSet, superSet: item.superSet, heavyWeight: item.heavyWeight});
  }
  return {userId: Number(selectedUserId), programId: selectedProgramId, dayIndex: selectedDayIndex,
    day: element("workout-day").value.trim() || "Моя тренировка", trainingDate: element("training-date").value,
    note: element("workout-note").value.trim(), exercises: items};
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
    element("workout-note").value = "";
    clearWorkoutTimer();
    loadRecovery();
    loadHistory();
    toast("Тренировка записана в историю. Список упражнений остался в конструкторе.");
  }).catch(function (error) { toast(error.message, true); });
}

function loadRecovery() {
  if (!selectedUserId) { element("recovery-list").innerHTML = '<p class="empty-message">Сначала создай профиль.</p>'; element("readiness-percent").textContent = "—"; element("readiness-explanation").textContent = "Сначала создай профиль."; currentRecovery = null; return; }
  request("/users/" + selectedUserId + "/recovery").then(function (data) {
    currentRecovery = data;
    element("readiness-percent").textContent = data.hasWorkout === false ? "—" : data.readinessPercent;
    var note = data.hasWorkout === false ? "Запиши тренировку, чтобы оценить восстановление. " : "";
    note += data.sleepHours === null ? "Сон не записан." : "Сон: " + data.sleepHours + " ч.";
    note += data.proteinLogged ? " Белок сегодня: " + data.proteinToday + " из " + data.proteinTarget + " г." : " Белок за сегодня не записан.";
    element("readiness-explanation").textContent = note;
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
  if (!selectedUserId) { element("nutrition-calories").textContent = "—"; nutritionTarget = 0; updateFoodSummary(); return; }
  request("/users/" + selectedUserId + "/nutrition?goal=" + selectedGoal).then(function (data) {
    element("nutrition-calories").textContent = data.calories;
    nutritionTarget = data.calories;
    element("burned-today").textContent = data.burnedToday + " ккал";
    updateFoodSummary();
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

function journalDay(date) {
  for (var i = 0; i < journalEntries.length; i++) if (journalEntries[i].date === date) return journalEntries[i];
  return null;
}

function fillJournalForms() {
  var sleep = journalDay(element("sleep-date").value);
  element("sleep-hours").value = sleep && sleep.sleepHours !== null ? sleep.sleepHours : "";
  renderMealsForDate();
}

function renderMealsForDate() {
  var entry = journalDay(element("food-date").value);
  var list = entry && entry.meals ? entry.meals : [];
  var types = {breakfast: "Завтрак", lunch: "Обед", dinner: "Ужин", snack: "Перекус", other: "Ранее введено"};
  var html = "";
  for (var type in types) {
    var group = "";
    for (var i = 0; i < list.length; i++) {
      var meal = list[i];
      if (meal.type !== type) continue;
      group += '<div class="meal-item"><div><strong>' + escapeText(meal.foodName) + '</strong><small>' + (meal.portionGrams ? meal.portionGrams + ' г · ' : '') + 'Б ' + meal.protein + ' · Ж ' + meal.fat + ' · У ' + meal.carbs + ' г</small></div><button class="icon-button delete" type="button" data-delete-meal="' + meal.id + '" aria-label="Удалить блюдо">×</button></div>';
    }
    if (group) html += '<div class="meal-group"><h3>' + types[type] + '</h3>' + group + '</div>';
  }
  if (!html && entry && entry.foodLogged) html = '<p class="helper-text">Ранее записанный итог: Б ' + entry.protein + ' · Ж ' + entry.fat + ' · У ' + entry.carbs + ' г. Новое блюдо добавится к нему.</p>';
  if (entry && entry.foodLogged) html += '<p class="meal-total">За день: Б ' + entry.protein + ' · Ж ' + entry.fat + ' · У ' + entry.carbs + ' г</p>';
  element("food-day-list").innerHTML = html || '<p class="empty-message">На эту дату блюд пока нет.</p>';
}

function updateFoodSummary() {
  var today = journalDay(localToday());
  var eaten = today && today.foodLogged ? today.protein * 4 + today.fat * 9 + today.carbs * 4 : 0;
  element("eaten-calories").textContent = today && today.foodLogged ? eaten + " ккал" : "Нет записи";
  element("remaining-calories").textContent = nutritionTarget ? Math.max(0, nutritionTarget - eaten) + " ккал" : "—";
}

function sleepButtons(date) {
  return '<div class="journal-actions"><button type="button" data-edit-sleep="' + date + '">Изменить</button><button type="button" data-delete-sleep="' + date + '">Удалить</button></div>';
}

function renderSleepHistory() {
  var html = "";
  for (var i = 0; i < journalEntries.length; i++) {
    var item = journalEntries[i];
    if (item.sleepHours == null) continue;
    html += '<div class="sleep-entry"><strong>' + escapeText(item.date) + '</strong><span>' + item.sleepHours + ' ч</span>' + sleepButtons(item.date) + '</div>';
  }
  element("sleep-history").innerHTML = html || '<p class="empty-message">Записей сна пока нет.</p>';
}

function loadJournal() {
  if (!selectedUserId) {
    journalEntries = [];
    nutritionTarget = 0;
    element("journal-list").innerHTML = '<p class="empty-message">Сначала создай профиль.</p>';
    renderSleepHistory();
    fillJournalForms();
    updateFoodSummary();
    return Promise.resolve();
  }
  return request("/users/" + selectedUserId + "/journal").then(function (data) {
    journalEntries = data;
    fillJournalForms();
    renderSleepHistory();
    updateFoodSummary();
    var html = "";
    for (var i = 0; i < data.length; i++) {
      var item = data[i];
      var count = item.meals ? item.meals.length : 0;
      html += '<div class="journal-entry"><strong>' + escapeText(item.date) + '</strong><span>' + (item.sleepHours == null ? 'Сон не записан' : 'Сон ' + item.sleepHours + ' ч') + '</span><span>' + (item.foodLogged ? count + ' блюд · Б ' + item.protein + ' · Ж ' + item.fat + ' · У ' + item.carbs + ' г' : 'Еда не записана') + '</span>' + (item.sleepHours == null ? '' : sleepButtons(item.date)) + '</div>';
    }
    element("journal-list").innerHTML = html || '<p class="empty-message">Записей пока нет.</p>';
  }).catch(function (error) { toast("Не удалось загрузить журнал: " + error.message, true); });
}

function handleSleepHistoryClick(event) {
  var edit = event.target.closest("[data-edit-sleep]");
  if (edit) {
    element("sleep-date").value = edit.getAttribute("data-edit-sleep");
    fillJournalForms();
    showPage("recovery", true);
    element("sleep-hours").focus();
    return;
  }
  var button = event.target.closest("[data-delete-sleep]");
  if (!button) return;
  if (!button.hasAttribute("data-armed")) {
    button.setAttribute("data-armed", "true");
    button.textContent = "Точно удалить?";
    return;
  }
  request("/users/" + selectedUserId + "/journal/" + button.getAttribute("data-delete-sleep") + "/sleep", "DELETE")
    .then(function () { return loadJournal(); })
    .then(function () { loadRecovery(); loadWeekSummary(); toast("Запись сна удалена"); })
    .catch(function (error) { toast(error.message, true); });
}

function saveSleep(event) {
  event.preventDefault();
  if (!selectedUserId) { toast("Сначала создай профиль", true); return; }
  var data = {date: element("sleep-date").value, sleepHours: Number(element("sleep-hours").value)};
  request("/users/" + selectedUserId + "/journal", "PUT", data).then(function () {
    return loadJournal();
  }).then(function () { loadRecovery(); loadWeekSummary(); toast("Сон сохранён в журнале"); })
    .catch(function (error) { toast(error.message, true); });
}

function saveFood(event) {
  event.preventDefault();
  if (!selectedUserId) { toast("Сначала создай профиль", true); return; }
  var data = {date: element("food-date").value, type: element("meal-type").value,
    foodName: element("food-name").value.trim(), portionGrams: Number(element("food-portion").value),
    protein: Number(element("food-protein").value), fat: Number(element("food-fat").value),
    carbs: Number(element("food-carbs").value)};
  request("/users/" + selectedUserId + "/meals", "POST", data).then(function () {
    element("food-name").value = "";
    element("food-portion").value = "";
    element("food-protein").value = "";
    element("food-fat").value = "";
    element("food-carbs").value = "";
    return loadJournal();
  }).then(function () { loadRecovery(); toast("Блюдо добавлено в дневник"); })
    .catch(function (error) { toast(error.message, true); });
}

function deleteMeal(button) {
  if (!selectedUserId) return;
  if (!button.hasAttribute("data-armed")) {
    button.setAttribute("data-armed", "true");
    button.textContent = "?";
    return;
  }
  var date = element("food-date").value;
  request("/users/" + selectedUserId + "/meals/" + date + "/" + button.getAttribute("data-delete-meal"), "DELETE")
    .then(function () { return loadJournal(); })
    .then(function () { loadRecovery(); toast("Блюдо удалено"); })
    .catch(function (error) { toast(error.message, true); });
}

function renderStrengthChoices() {
  var select = element("strength-exercise");
  var previous = select.value;
  var html = '<option value="">Выбери упражнение</option>';
  for (var i = 0; i < exercises.length; i++) {
    if (exercises[i].unit === "minutes") continue;
    html += '<option value="' + escapeText(exercises[i].id) + '">' + escapeText(exercises[i].name) + '</option>';
  }
  select.innerHTML = html;
  select.value = previous;
  renderStrength();
}

function loadStrength() {
  if (!selectedUserId) {
    strengthData = {records: [], goals: []};
    renderStrength();
    return;
  }
  request("/users/" + selectedUserId + "/strength").then(function (data) {
    strengthData = data;
    renderStrength();
  }).catch(function (error) { toast("Не удалось загрузить прогресс: " + error.message, true); });
}

function selectedStrengthGoal(id) {
  for (var i = 0; i < strengthData.goals.length; i++) {
    if (strengthData.goals[i].exerciseId === id) return strengthData.goals[i];
  }
  return null;
}

function renderStrength() {
  var id = element("strength-exercise").value;
  var name = element("strength-exercise").selectedOptions[0];
  element("strength-chart-title").textContent = id && name ? name.textContent : "Выбери упражнение";
  var goal = selectedStrengthGoal(id);
  element("strength-target").value = goal ? goal.targetKg : "";
  var matching = [];
  for (var i = 0; i < strengthData.records.length; i++) {
    if (strengthData.records[i].exerciseId === id) matching.push(strengthData.records[i]);
  }
  var best = 0;
  for (var b = 0; b < matching.length; b++) best = Math.max(best, matching[b].weightKg);
  element("strength-best").textContent = matching.length ? "Личный рекорд: " + best + " кг · последняя запись: " + matching[0].weightKg + " кг" : "Для этого упражнения пока нет записей.";
  var forecast = "Запиши цель для этого упражнения.";
  if (goal) {
    if (goal.estimatedWeeks === 0) forecast = "Цель " + goal.targetKg + " кг уже достигнута по последней записи.";
    else if (goal.estimatedWeeks !== null) forecast = "При прежнем темпе роста до " + goal.targetKg + " кг примерно " + goal.estimatedWeeks + " нед. Это только оценка.";
    else if (goal.recordCount < 3) forecast = "Цель: " + goal.targetKg + " кг. Добавь минимум 3 записи за 2 недели для примерного срока.";
    else forecast = "Цель: " + goal.targetKg + " кг. Пока нет устойчивого роста для прогноза.";
  }
  element("strength-forecast").textContent = forecast;
  var rows = "";
  for (var j = 0; j < matching.length; j++) {
    var item = matching[j];
    rows += '<div class="strength-row"><span>' + escapeText(item.date) + '</span><strong>' + item.weightKg + ' кг' + (item.weightKg === best ? ' ★' : '') + '</strong><span>' + item.sets + ' × ' + item.reps + '</span><button class="record-edit" type="button" data-edit-strength="' + item.id + '">Изменить</button><button class="icon-button delete" type="button" data-delete-strength="' + item.id + '" aria-label="Удалить запись">×</button></div>';
  }
  element("strength-records").innerHTML = rows || '<p class="empty-message">Для этого упражнения записей пока нет.</p>';
  drawStrengthChart(matching, goal);
}

function drawStrengthChart(records, goal) {
  var svg = element("strength-chart");
  if (records.length === 0) {
    svg.innerHTML = '<text x="320" y="110" text-anchor="middle" fill="#96aa96" font-size="17">Добавь первый рабочий вес</text>';
    return;
  }
  var sorted = records.slice().sort(function (a, b) { return a.date.localeCompare(b.date) || a.id - b.id; });
  var low = sorted[0].weightKg, high = sorted[0].weightKg;
  for (var i = 0; i < sorted.length; i++) {
    low = Math.min(low, sorted[i].weightKg);
    high = Math.max(high, sorted[i].weightKg);
  }
  if (goal) { low = Math.min(low, goal.targetKg); high = Math.max(high, goal.targetKg); }
  low = Math.max(0, low - 5);
  high += 5;
  var first = new Date(sorted[0].date + "T12:00:00").getTime();
  var last = new Date(sorted[sorted.length - 1].date + "T12:00:00").getTime();
  var points = [];
  for (var j = 0; j < sorted.length; j++) {
    var day = new Date(sorted[j].date + "T12:00:00").getTime();
    var x = first === last ? 320 : 40 + (day - first) * 560 / (last - first);
    var y = 165 - (sorted[j].weightKg - low) * 130 / (high - low);
    points.push({x: x, y: y, weight: sorted[j].weightKg});
  }
  var html = '<line x1="40" y1="165" x2="600" y2="165" stroke="#405442" stroke-width="2"/>';
  if (goal) {
    var targetY = 165 - (goal.targetKg - low) * 130 / (high - low);
    html += '<line x1="40" y1="' + targetY + '" x2="600" y2="' + targetY + '" stroke="#ffbd73" stroke-width="2" stroke-dasharray="8 6"/><text x="600" y="' + (targetY - 5) + '" text-anchor="end" fill="#ffbd73" font-size="13">Цель ' + goal.targetKg + ' кг</text>';
  }
  if (points.length > 1) {
    var coordinates = [];
    for (var k = 0; k < points.length; k++) coordinates.push(points[k].x + "," + points[k].y);
    html += '<polyline points="' + coordinates.join(" ") + '" fill="none" stroke="#bbf25a" stroke-width="3"/>';
  }
  for (var p = 0; p < points.length; p++) html += '<circle cx="' + points[p].x + '" cy="' + points[p].y + '" r="5" fill="#bbf25a"/>';
  html += '<text x="40" y="198" fill="#96aa96" font-size="12">' + escapeText(sorted[0].date) + '</text>';
  html += '<text x="600" y="198" text-anchor="end" fill="#96aa96" font-size="12">' + escapeText(sorted[sorted.length - 1].date) + '</text>';
  svg.innerHTML = html;
}

function saveStrength(event) {
  event.preventDefault();
  if (!selectedUserId) { toast("Сначала создай профиль", true); return; }
  var data = {exerciseId: element("strength-exercise").value, date: element("strength-date").value,
    weightKg: Number(element("strength-weight").value), sets: Number(element("strength-sets").value),
    reps: Number(element("strength-reps").value)};
  var path = "/users/" + selectedUserId + "/strength" + (strengthEditId ? "/" + strengthEditId : "");
  var editing = strengthEditId !== 0;
  request(path, editing ? "PUT" : "POST", data).then(function () {
    element("strength-weight").value = "";
    resetStrengthEdit();
    loadStrength();
    toast(editing ? "Запись исправлена" : "Рабочий вес записан");
  }).catch(function (error) { toast(error.message, true); });
}

function editStrength(id) {
  for (var i = 0; i < strengthData.records.length; i++) {
    var item = strengthData.records[i];
    if (item.id !== id) continue;
    strengthEditId = id;
    element("strength-exercise").value = item.exerciseId;
    element("strength-date").value = item.date;
    element("strength-weight").value = item.weightKg;
    element("strength-sets").value = item.sets;
    element("strength-reps").value = item.reps;
    element("strength-form-title").textContent = "Изменить запись";
    element("strength-save").textContent = "Сохранить изменение";
    element("strength-cancel").hidden = false;
    element("strength-form").scrollIntoView({behavior: "smooth", block: "start"});
    element("strength-weight").focus();
    return;
  }
}

function resetStrengthEdit() {
  strengthEditId = 0;
  element("strength-form-title").textContent = "Рабочий вес";
  element("strength-save").textContent = "Записать результат";
  element("strength-cancel").hidden = true;
}

function saveStrengthGoal(event) {
  event.preventDefault();
  if (!selectedUserId || !element("strength-exercise").value) { toast("Выбери профиль и упражнение", true); return; }
  var data = {exerciseId: element("strength-exercise").value, targetKg: Number(element("strength-target").value)};
  request("/users/" + selectedUserId + "/strength/goals", "PUT", data).then(function () {
    loadStrength();
    toast("Цель сохранена");
  }).catch(function (error) { toast(error.message, true); });
}

function deleteStrength(button) {
  if (!button.hasAttribute("data-armed")) {
    button.setAttribute("data-armed", "true");
    button.textContent = "?";
    return;
  }
  request("/users/" + selectedUserId + "/strength/" + button.getAttribute("data-delete-strength"), "DELETE")
    .then(function () { loadStrength(); toast("Запись удалена"); })
    .catch(function (error) { toast(error.message, true); });
}

function loadBodyWeights() {
  if (!selectedUserId) { bodyWeightData = []; renderBodyWeights(); return; }
  return request("/users/" + selectedUserId + "/body-weight").then(function (data) {
    bodyWeightData = data;
    renderBodyWeights();
  }).catch(function (error) { toast("Не удалось загрузить вес тела: " + error.message, true); });
}

function renderBodyWeights() {
  var svg = element("body-weight-chart");
  if (bodyWeightData.length === 0) {
    svg.innerHTML = '<text x="320" y="110" text-anchor="middle" fill="#96aa96" font-size="17">Добавь первый вес тела</text>';
    element("body-weight-list").innerHTML = '<p class="empty-message">Записей веса пока нет.</p>';
    return;
  }
  var first = new Date(bodyWeightData[0].date + "T12:00:00").getTime();
  var last = new Date(bodyWeightData[bodyWeightData.length - 1].date + "T12:00:00").getTime();
  var low = bodyWeightData[0].weightKg, high = low;
  for (var i = 0; i < bodyWeightData.length; i++) {
    low = Math.min(low, bodyWeightData[i].weightKg);
    high = Math.max(high, bodyWeightData[i].weightKg);
  }
  low -= 2;
  high += 2;
  var points = [];
  var html = '<line x1="40" y1="165" x2="600" y2="165" stroke="#405442" stroke-width="2"/>';
  for (var j = 0; j < bodyWeightData.length; j++) {
    var item = bodyWeightData[j];
    var day = new Date(item.date + "T12:00:00").getTime();
    var x = first === last ? 320 : 40 + (day - first) * 560 / (last - first);
    var y = 165 - (item.weightKg - low) * 130 / (high - low);
    points.push(x + "," + y);
    html += '<circle cx="' + x + '" cy="' + y + '" r="5" fill="#bbf25a"/>';
  }
  if (points.length > 1) html += '<polyline points="' + points.join(" ") + '" fill="none" stroke="#bbf25a" stroke-width="3"/>';
  html += '<text x="40" y="198" fill="#96aa96" font-size="12">' + escapeText(bodyWeightData[0].date) + '</text>';
  html += '<text x="600" y="198" text-anchor="end" fill="#96aa96" font-size="12">' + escapeText(bodyWeightData[bodyWeightData.length - 1].date) + '</text>';
  svg.innerHTML = html;
  var rows = "";
  for (var k = bodyWeightData.length - 1; k >= 0; k--) {
    var record = bodyWeightData[k];
    rows += '<div class="body-weight-row"><span>' + escapeText(record.date) + '</span><strong>' + record.weightKg + ' кг</strong><button type="button" data-delete-body-weight="' + escapeText(record.date) + '">Удалить</button></div>';
  }
  element("body-weight-list").innerHTML = rows;
}

function saveBodyWeight(event) {
  event.preventDefault();
  if (!selectedUserId) { toast("Сначала создай профиль", true); return; }
  var data = {date: element("body-weight-date").value, weightKg: Number(element("body-weight-value").value)};
  request("/users/" + selectedUserId + "/body-weight", "PUT", data)
    .then(function () { return loadBodyWeights(); })
    .then(function () { toast("Вес тела записан"); })
    .catch(function (error) { toast(error.message, true); });
}

function deleteBodyWeight(button) {
  if (!button.hasAttribute("data-armed")) {
    button.setAttribute("data-armed", "true");
    button.textContent = "Точно удалить?";
    return;
  }
  request("/users/" + selectedUserId + "/body-weight/" + button.getAttribute("data-delete-body-weight"), "DELETE")
    .then(function () { return loadBodyWeights(); })
    .then(function () { toast("Запись веса удалена"); })
    .catch(function (error) { toast(error.message, true); });
}

function exportDiary() {
  if (!selectedUserId) { toast("Сначала создай профиль", true); return; }
  request("/users/" + selectedUserId + "/export").then(function (data) {
    var blob = new Blob([JSON.stringify(data, null, 2)], {type: "application/json"});
    var url = URL.createObjectURL(blob);
    var link = document.createElement("a");
    link.href = url;
    link.download = "PotentialMan-" + selectedUserId + "-" + localToday() + ".json";
    document.body.appendChild(link);
    link.click();
    link.remove();
    setTimeout(function () { URL.revokeObjectURL(url); }, 1000);
    toast("Дневник скачан");
  }).catch(function (error) { toast(error.message, true); });
}

function loadHistory() {
  if (!selectedUserId) { historyData = []; element("history-list").innerHTML = '<p class="empty-message">Сначала создай профиль.</p>'; element("week-summary").innerHTML = '<p class="empty-message">Сначала создай профиль.</p>'; return; }
  loadWeekSummary();
  request("/users/" + selectedUserId + "/history").then(function (data) {
    historyData = data;
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
      html += '<article class="history-card"><div class="history-item"><div><span class="overline">' + escapeText(time) + '</span><h3>' + escapeText(workout.day) + '</h3><p>' + escapeText(groups.join(" · ")) + ' · ' + workout.exercises.length + ' упр.</p></div><div class="history-right"><span class="history-calories">' + workout.calories + ' ккал</span><button class="secondary-button history-toggle" type="button" data-toggle="' + workout.id + '" aria-expanded="false">Упражнения ↓</button><button class="secondary-button" type="button" data-repeat="' + workout.id + '">Повторить ↗</button><button class="icon-button delete" type="button" data-delete="' + workout.id + '" aria-label="Удалить тренировку">×</button></div></div><div class="history-details" id="history-details-' + workout.id + '" hidden>';
      for (var j = 0; j < workout.exercises.length; j++) {
        var exercise = workout.exercises[j];
        var modifiers = [];
        if (exercise.dropSet) modifiers.push("дропсет");
        if (exercise.superSet) modifiers.push("суперсет");
        if (exercise.heavyWeight) modifiers.push("тяжёлый вес");
        var durations = [];
        var savedSeconds = exercise.setSeconds || [];
        for (var s = 0; s < savedSeconds.length; s++) durations.push(clockText(savedSeconds[s]));
        if (durations.length) modifiers.push("Время подходов: " + durations.join(", "));
        html += '<div class="history-exercise"><strong>' + escapeText(exercise.name) + '</strong><span>' + (exercise.unit === "minutes" ? exercise.reps + ' мин' : exercise.sets + ' × ' + exercise.reps + (exercise.weightKg ? ' · ' + exercise.weightKg + ' кг' : '')) + '</span><small>' + escapeText(modifiers.join(" · ")) + '</small></div>';
      }
      if (workout.note) html += '<p class="history-note">Заметка: ' + escapeText(workout.note) + '</p>';
      html += '</div></article>';
    }
    html += '</div></section>';
    element("history-list").innerHTML = html;
  }).catch(function (error) { toast(error.message, true); });
}

function loadWeekSummary() {
  if (!selectedUserId) return;
  request("/users/" + selectedUserId + "/week").then(function (data) {
    element("week-summary").innerHTML = '<div><strong>' + data.workouts + '</strong><span>тренировок</span></div>' +
      '<div><strong>' + data.restDays + '</strong><span>дней отдыха</span></div>' +
      '<div><strong>' + data.sleepHours + ' ч</strong><span>сна за ' + data.sleepNights + ' ночей</span></div>' +
      '<div><strong>' + data.cardioMinutes + ' мин</strong><span>кардио за неделю</span></div>';
  }).catch(function (error) { toast("Не удалось загрузить итоги недели: " + error.message, true); });
}

function repeatWorkout(id) {
  for (var i = 0; i < historyData.length; i++) {
    var workout = historyData[i];
    if (workout.id !== id) continue;
    selectedExercises = [];
    for (var j = 0; j < workout.exercises.length; j++) {
      var item = workout.exercises[j];
      selectedExercises.push({id: item.id, name: item.name, group: item.group, unit: item.unit || "reps",
        sets: item.sets, reps: item.reps, weightKg: item.weightKg || 0,
        dropSet: !!item.dropSet, superSet: !!item.superSet, heavyWeight: !!item.heavyWeight, setSeconds: []});
    }
    selectedDayIndex = -1;
    element("workout-day").value = workout.day;
    element("training-date").value = localToday();
    element("workout-note").value = "";
    renderSelected();
    renderWeeklyPlan();
    showPage("workout", true);
    previewWorkout();
    toast("Прошлая тренировка загружена. Проверь вес и запиши новую дату.");
    return;
  }
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
  restOverrides = readRestOverrides();
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
  loadJournal();
  loadRecovery();
  loadNutrition();
  loadHistory();
  loadStrength();
  loadBodyWeights();
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
element("load-starter").addEventListener("click", loadStarter);
element("delete-template").addEventListener("click", deleteTemplate);
element("clear-builder").addEventListener("click", function () {
  selectedExercises = [];
  renderSelected();
  previewWorkout();
  toast("Список упражнений очищен. Сохранённый шаблон дня не изменился.");
});
element("exercise-search").addEventListener("input", renderCatalog);
element("clear-search").addEventListener("click", function () {
  element("exercise-search").value = "";
  element("muscle-filter").value = "";
  favoritesOnly = false;
  renderCatalog();
  element("exercise-search").focus();
});
element("favorites-only").addEventListener("click", function () { favoritesOnly = !favoritesOnly; renderCatalog(); });
element("muscle-filter").addEventListener("change", renderCatalog);
element("exercise-list").addEventListener("click", function (event) {
  var favorite = event.target.closest("[data-favorite]");
  if (favorite) { toggleFavorite(favorite.getAttribute("data-favorite")); return; }
  var button = event.target.closest("[data-add]");
  if (button) addExercise(button.getAttribute("data-add"));
});
element("sleep-form").addEventListener("submit", saveSleep);
element("sleep-history").addEventListener("click", handleSleepHistoryClick);
element("journal-list").addEventListener("click", handleSleepHistoryClick);
element("food-form").addEventListener("submit", saveFood);
element("strength-form").addEventListener("submit", saveStrength);
element("body-weight-form").addEventListener("submit", saveBodyWeight);
element("body-weight-list").addEventListener("click", function (event) {
  var button = event.target.closest("[data-delete-body-weight]");
  if (button) deleteBodyWeight(button);
});
element("export-diary").addEventListener("click", exportDiary);
element("timer-start").addEventListener("click", startWorkoutSet);
element("timer-stop").addEventListener("click", stopWorkoutSet);
element("timer-skip").addEventListener("click", skipWorkoutRest);
element("timer-reset").addEventListener("click", clearWorkoutTimer);
element("timer-rest-custom").addEventListener("change", saveCustomRest);
element("timer-rest-auto").addEventListener("click", function () { element("timer-rest-custom").value = ""; saveCustomRest(); });
element("timer-sound").addEventListener("change", function () {
  localStorage.setItem("potentialmanTimerSound", this.checked ? "on" : "off");
  if (this.checked) prepareTimerAudio();
  else stopTimerSound();
});
element("timer-sound").checked = localStorage.getItem("potentialmanTimerSound") !== "off";
setInterval(tickWorkoutTimer, 250);
element("strength-goal-form").addEventListener("submit", saveStrengthGoal);
element("strength-exercise").addEventListener("change", function () { resetStrengthEdit(); renderStrength(); });
element("strength-cancel").addEventListener("click", resetStrengthEdit);
element("strength-records").addEventListener("click", function (event) {
  var edit = event.target.closest("[data-edit-strength]");
  if (edit) { editStrength(Number(edit.getAttribute("data-edit-strength"))); return; }
  var button = event.target.closest("[data-delete-strength]");
  if (button) deleteStrength(button);
});
element("food-day-list").addEventListener("click", function (event) {
  var button = event.target.closest("[data-delete-meal]");
  if (button) deleteMeal(button);
});
element("sleep-date").addEventListener("change", fillJournalForms);
element("food-date").addEventListener("change", fillJournalForms);
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
  var repeat = event.target.closest("[data-repeat]");
  if (repeat) { repeatWorkout(Number(repeat.getAttribute("data-repeat"))); return; }
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
element("sleep-date").value = localToday();
element("sleep-date").max = localToday();
element("food-date").value = localToday();
element("food-date").max = localToday();
element("strength-date").value = localToday();
element("strength-date").max = localToday();
element("body-weight-date").value = localToday();
element("body-weight-date").max = localToday();
showPage(location.hash.substring(1), false);
loadUsers();
loadExercises();

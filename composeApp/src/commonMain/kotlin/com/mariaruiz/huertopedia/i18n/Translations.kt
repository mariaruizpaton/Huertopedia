package com.mariaruiz.huertopedia.i18n

import androidx.compose.runtime.staticCompositionLocalOf

interface AppStrings {
    val appName: String
    val loginWelcome: String
    val loginCreateAccount: String
    val loginGoogle: String
    val loginSignIn: String
    val loginRegister: String
    val loginName: String
    val loginEmail: String
    val loginPassword: String
    val loginAccept: String
    val changeLanguage: String
    val profileTitle: String
    val homeWelcome: String 
    val welcomeSubtitle: String
    val logoutButton: String
    val homeGardenCard: String
    val homeGardenDesc: String
    val homeWikiCard: String
    val homeWikiDesc: String
    val homeLastActivity: String
    val homeActivitySample: String
    val searchPlaceholder: String
    val wikiTitle: String
    val viewProfile: String

    // Wiki
    val wikiCategoryAll: String
    val wikiCategoryVegetables: String
    val wikiCategoryFruits: String
    val wikiCategoryHerbs: String
    val wikiNoPhoto: String

    // Detail
    val detailTitle: String
    val detailScientificName: String
    val detailSowing: String
    val detailHarvest: String
    val detailWatering: String
    val detailFertilizer: String
    val detailTemperature: String
    val detailCare: String
    val detailFriends: String
    val detailEnemies: String
    val detailNotSpecified: String
    val detailNoneKnown: String
    val detailBack: String

    // Garden
    val gardenTitle: String
    val gardenAddPlanter: String
    val gardenNewPlanter: String
    val gardenPlanterNamePlaceholder: String
    val gardenRows: String
    val gardenCols: String
    val gardenCreate: String
    val gardenCancel: String
    val gardenManagePots: String
    val gardenManagePotsQuestion: String
    val gardenActionPlant: String
    val gardenActionSow: String
    val gardenActionHarvest: String
    val gardenSelectPlant: String
    val gardenConfirm: String
    val gardenDelete: String
    val gardenDeleteConfirmQuestion: String
    val gardenNoPlanters: String
    val gardenPlanterSize: String
    val gardenCropLog: String
    val gardenActivityButton: String
    val gardenSelectionErrorTitle: String
    val gardenSelectionErrorText: String
    val gardenOk: String
    val gardenEditNameTitle: String
    val gardenNewNameLabel: String
    val gardenSave: String

    // Theme (NUEVO)
    val themeTitle: String
    val themeLight: String
    val themeDark: String
    val themeSystem: String

    // CropLog
    val cropLogTitle: String
    val cropLogEntry: String
    val cropLogStatus: String
    val cropLogNoEntries: String
    val cropLogRecently: String
    val cropLogUnknownDate: String
    val cropLogNewEntry: String
    val cropLogAddEntry: String
    val cropLogEventType: String
    val cropLogIrrigationMethod: String
    val cropLogIrrigationDuration: String
    val cropLogSymptoms: String
    val cropLogWriteNotes: String
    val cropLogAddPhoto: String
    val cropLogObservations: String
    val cropLogEventToday: String
    
    // Event Types
    val eventNotes: String
    val eventIrrigation: String
    val eventGermination: String
    val eventDisease: String
    val eventFertilization: String
    val eventTrellising: String
    val eventWeeding: String
    
    // Irrigation Types
    val irrigationManual: String
    val irrigationDrip: String
    val irrigationRain: String

    // Errors
    val errorInvalidCredentials: String
    val errorUserNotFound: String
    val errorEmailAlreadyInUse: String
    val errorWeakPassword: String
    val errorInvalidEmail: String
    val errorNetwork: String
    val errorUnknown: String
    val errorFieldsEmpty: String
    val errorNameEmpty: String
}

object EnStrings : AppStrings {
    override val appName = "Huertopedia"
    override val loginWelcome = "Welcome Back"
    override val loginCreateAccount = "Create your Account"
    override val loginGoogle = "Sign in with Google"
    override val loginSignIn = "Sign In"
    override val loginRegister = "Register"
    override val loginName = "Full Name"
    override val loginEmail = "Email Address"
    override val loginPassword = "Password"
    override val loginAccept = "Accept"
    override val changeLanguage = "Language: EN"
    override val profileTitle = "My Profile"
    override val homeWelcome = "Hello, {0}!"
    override val welcomeSubtitle = "Your garden is growing today 🌿"
    override val logoutButton = "Logout"
    override val homeGardenCard = "Garden Management"
    override val homeGardenDesc = "See your plots and log activities"
    override val homeWikiCard = "Plant Encyclopedia"
    override val homeWikiDesc = "Information about tomatoes, lettuce and more"
    override val homeLastActivity = "Last recorded activity"
    override val homeActivitySample = "Yesterday: Watering in Plot 1"
    override val searchPlaceholder = "Search plants..."
    override val wikiTitle = "Wiki"
    override val viewProfile = "View profile"

    override val wikiCategoryAll = "All"
    override val wikiCategoryVegetables = "Vegetables"
    override val wikiCategoryFruits = "Fruits"
    override val wikiCategoryHerbs = "Herbs"
    override val wikiNoPhoto = "No photo"

    override val detailTitle = "Plant Details"
    override val detailScientificName = "Scientific Name"
    override val detailSowing = "Sowing"
    override val detailHarvest = "Harvest"
    override val detailWatering = "Watering"
    override val detailFertilizer = "Fertilizer"
    override val detailTemperature = "Optimal Temperature"
    override val detailCare = "Care Tips"
    override val detailFriends = "Companion plants"
    override val detailEnemies = "Combative plants"
    override val detailNotSpecified = "Not specified"
    override val detailNoneKnown = "None known"
    override val detailBack = "Back"

    override val gardenTitle = "My Gardeners"
    override val gardenAddPlanter = "Add Gardener"
    override val gardenNewPlanter = "New Gardener"
    override val gardenPlanterNamePlaceholder = "Name (e.g. Kitchen Window)"
    override val gardenRows = "Rows"
    override val gardenCols = "Columns"
    override val gardenCreate = "Create"
    override val gardenCancel = "Cancel"
    override val gardenManagePots = "Manage flowerpots"
    override val gardenManagePotsQuestion = "What do you want to do in the {0} flowerpots?"
    override val gardenActionPlant = "Plant"
    override val gardenActionSow = "Sow"
    override val gardenActionHarvest = "Harvest"
    override val gardenSelectPlant = "Select plant"
    override val gardenConfirm = "Confirm"
    override val gardenDelete = "Delete"
    override val gardenDeleteConfirmQuestion = "Delete '{0}'?"
    override val gardenNoPlanters = "No gardeners yet."
    override val gardenPlanterSize = "{0} x {1}"
    override val gardenCropLog = "Crop Log"
    override val gardenActivityButton = "Activity in {0}"
    override val gardenSelectionErrorTitle = "Invalid selection"
    override val gardenSelectionErrorText = "You cannot select occupied and empty pots at the same time."
    override val gardenOk = "Got it"
    override val gardenEditNameTitle = "Edit name"
    override val gardenNewNameLabel = "New name"
    override val gardenSave = "Save"

    override val themeTitle = "App Theme"
    override val themeLight = "Light"
    override val themeDark = "Dark"
    override val themeSystem = "System"

    override val cropLogTitle = "Log: {0}"
    override val cropLogEntry = "Pot {0},{1}: {2}"
    override val cropLogStatus = "{0} on {1}"
    override val cropLogNoEntries = "No activities recorded in this gardener."
    override val cropLogRecently = "recently"
    override val cropLogUnknownDate = "Unknown date"
    override val cropLogNewEntry = "New Entry"
    override val cropLogAddEntry = "Add entry"
    override val cropLogEventType = "Event Type"
    override val cropLogIrrigationMethod = "Irrigation Method"
    override val cropLogIrrigationDuration = "Duration: {0} min"
    override val cropLogSymptoms = "Symptoms..."
    override val cropLogWriteNotes = "Write your notes..."
    override val cropLogAddPhoto = "Add photo"
    override val cropLogObservations = "Observations (Optional)"
    override val cropLogEventToday = "Event: '{0}' with today's date."
    
    override val eventNotes = "Notes"
    override val eventIrrigation = "Irrigation"
    override val eventGermination = "Germination"
    override val eventDisease = "Disease"
    override val eventFertilization = "Fertilization"
    override val eventTrellising = "Trellising"
    override val eventWeeding = "Weeding"
    
    override val irrigationManual = "Manual"
    override val irrigationDrip = "Drip"
    override val irrigationRain = "Rain"

    override val errorInvalidCredentials = "Invalid email or password."
    override val errorUserNotFound = "No account found with this email."
    override val errorEmailAlreadyInUse = "This email is already registered."
    override val errorWeakPassword = "Password is too weak."
    override val errorInvalidEmail = "Invalid email format."
    override val errorNetwork = "No internet connection."
    override val errorUnknown = "An unexpected error occurred."
    override val errorFieldsEmpty = "Please fill in all fields."
    override val errorNameEmpty = "Name cannot be empty."
}

object EsStrings : AppStrings {
    override val appName = "Huertopedia"
    override val loginWelcome = "Bienvenido"
    override val loginCreateAccount = "Crear Cuenta"
    override val loginGoogle = "Acceder con Google"
    override val loginSignIn = "Iniciar Sesión"
    override val loginRegister = "Registrarse"
    override val loginName = "Nombre"
    override val loginEmail = "Email"
    override val loginPassword = "Contraseña"
    override val loginAccept = "Aceptar"
    override val changeLanguage = "Idioma: ES"
    override val profileTitle = "Mi Perfil"
    override val homeWelcome = "¡Hola, {0}!"
    override val welcomeSubtitle = "Tu huerto está creciendo hoy 🌿"
    override val logoutButton = "Cerrar sesión"
    override val homeGardenCard = "Gestión del Huerto"
    override val homeGardenDesc = "Mira tus parcelas y registra actividades"
    override val homeWikiCard = "Enciclopedia de Cultivos"
    override val homeWikiDesc = "Información sobre tomates, lechugas y más"
    override val homeLastActivity = "Última actividad registrada"
    override val homeActivitySample = "Ayer: Riego en Jardinera 1"
    override val searchPlaceholder = "Buscar plantas..."
    override val wikiTitle = "Wiki"
    override val viewProfile = "Ver perfil"

    override val wikiCategoryAll = "Todo"
    override val wikiCategoryVegetables = "Hortalizas"
    override val wikiCategoryFruits = "Frutas"
    override val wikiCategoryHerbs = "Hierbas"
    override val wikiNoPhoto = "Sin foto"

    override val detailTitle = "Detalles de la planta"
    override val detailScientificName = "Nombre Científico"
    override val detailSowing = "Siembra"
    override val detailHarvest = "Recolección"
    override val detailWatering = "Riego"
    override val detailFertilizer = "Abono"
    override val detailTemperature = "Temperatura Óptima"
    override val detailCare = "Cuidados"
    override val detailFriends = "Plantas amigas"
    override val detailEnemies = "Plantas enemigas"
    override val detailNotSpecified = "No especificado"
    override val detailNoneKnown = "Ninguna conocida"
    override val detailBack = "Volver"

    override val gardenTitle = "Mis Jardineras"
    override val gardenAddPlanter = "Añadir Jardinera"
    override val gardenNewPlanter = "Nueva Jardinera"
    override val gardenPlanterNamePlaceholder = "Nombre (ej: Terraza)"
    override val gardenRows = "Filas"
    override val gardenCols = "Columnas"
    override val gardenCreate = "Crear"
    override val gardenCancel = "Cancelar"
    override val gardenManagePots = "Gestionar macetas"
    override val gardenManagePotsQuestion = "¿Qué quieres hacer en las {0} macetas?"
    override val gardenActionPlant = "Plantar"
    override val gardenActionSow = "Sembrar"
    override val gardenActionHarvest = "Recolectar"
    override val gardenSelectPlant = "Selecciona planta"
    override val gardenConfirm = "Confirmar"
    override val gardenDelete = "Eliminar"
    override val gardenDeleteConfirmQuestion = "¿Borrar '{0}'?"
    override val gardenNoPlanters = "No tienes jardineras aún."
    override val gardenPlanterSize = "{0} x {1}"
    override val gardenCropLog = "Diario de Cultivo"
    override val gardenActivityButton = "Actividad en {0}"
    override val gardenSelectionErrorTitle = "Selección inválida"
    override val gardenSelectionErrorText = "No puedes seleccionar macetas ocupadas y vacías al mismo tiempo."
    override val gardenOk = "Entendido"
    override val gardenEditNameTitle = "Editar nombre"
    override val gardenNewNameLabel = "Nuevo nombre"
    override val gardenSave = "Guardar"

    override val themeTitle = "Tema de la App"
    override val themeLight = "Claro"
    override val themeDark = "Oscuro"
    override val themeSystem = "Sistema"

    override val cropLogTitle = "Diario: {0}"
    override val cropLogEntry = "Maceta {0},{1}: {2}"
    override val cropLogStatus = "{0} el {1}"
    override val cropLogNoEntries = "No hay actividades registradas en esta jardinera."
    override val cropLogRecently = "recientemente"
    override val cropLogUnknownDate = "Fecha desconocida"
    override val cropLogNewEntry = "Nueva Entrada"
    override val cropLogAddEntry = "Añadir entrada"
    override val cropLogEventType = "Tipo de Evento"
    override val cropLogIrrigationMethod = "Método de Riego"
    override val cropLogIrrigationDuration = "Duración: {0} min"
    override val cropLogSymptoms = "Síntomas..."
    override val cropLogWriteNotes = "Escribe tus notas..."
    override val cropLogAddPhoto = "Añadir foto"
    override val cropLogObservations = "Observaciones (Opcional)"
    override val cropLogEventToday = "Evento: '{0}' con fecha de hoy."
    
    override val eventNotes = "Notas"
    override val eventIrrigation = "Riego"
    override val eventGermination = "Germinación"
    override val eventDisease = "Enfermedad"
    override val eventFertilization = "Fertilización"
    override val eventTrellising = "Entutorado"
    override val eventWeeding = "Eliminación adventicias"
    
    override val irrigationManual = "Manual"
    override val irrigationDrip = "Goteo"
    override val irrigationRain = "Lluvia"

    override val errorInvalidCredentials = "El correo o la contraseña son incorrectos."
    override val errorUserNotFound = "No existe ninguna cuenta con este correo."
    override val errorEmailAlreadyInUse = "Este correo ya está registrado."
    override val errorWeakPassword = "La contraseña es demasiado corta."
    override val errorInvalidEmail = "El formato del correo no es válido."
    override val errorNetwork = "No hay conexión a internet."
    override val errorUnknown = "Ha ocurrido un error inesperado."
    override val errorFieldsEmpty = "Rellena todos los campos para continuar."
    override val errorNameEmpty = "El nombre no puede estar vacío."
}

val LocalStrings = staticCompositionLocalOf<AppStrings> { EsStrings }

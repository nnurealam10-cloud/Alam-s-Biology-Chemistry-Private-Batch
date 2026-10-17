package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AlamsDatabase
import com.example.data.local.entity.*
import com.example.data.repository.AlamsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject

sealed class CurrentScreen {
    object Home : CurrentScreen()
    object Classes : CurrentScreen()
    object Exams : CurrentScreen()
    object Community : CurrentScreen()
    object Fees : CurrentScreen()
    object AdminConsole : CurrentScreen()
    object Profile : CurrentScreen()
    data class ExamTaking(val exam: ExamEntity) : CurrentScreen()
    data class ExamResult(val attempt: ExamAttemptEntity, val exam: ExamEntity) : CurrentScreen()
    data class DiscussionThread(val message: DiscussionMessageEntity) : CurrentScreen()
    data class ReceiptView(val submission: PaymentSubmissionEntity) : CurrentScreen()
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    val repository: AlamsRepository

    init {
        val database = AlamsDatabase.getDatabase(application)
        repository = AlamsRepository(database)
    }

    // Active Navigation Screen
    private val _currentScreen = MutableStateFlow<CurrentScreen>(CurrentScreen.Home)
    val currentScreen: StateFlow<CurrentScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: CurrentScreen) {
        _currentScreen.value = screen
    }

    // Student Profile
    val profile: StateFlow<StudentProfileEntity?> = repository.studentProfile
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            StudentProfileEntity(
                name = "তানভীর হোসেন",
                phone = "01712345678",
                roll = "SSC-2026-042",
                batch = "SSC",
                role = "STUDENT"
            )
        )

    fun switchBatch(batch: String) {
        viewModelScope.launch {
            repository.switchBatch(batch)
        }
    }

    fun toggleRole() {
        val current = profile.value?.role ?: "STUDENT"
        val next = if (current == "STUDENT") "ADMIN" else "STUDENT"
        viewModelScope.launch {
            repository.toggleRole(next)
            if (next == "ADMIN") {
                _currentScreen.value = CurrentScreen.AdminConsole
            } else {
                _currentScreen.value = CurrentScreen.Home
            }
        }
    }

    fun togglePseudonym(usePseudonym: Boolean) {
        viewModelScope.launch {
            repository.togglePseudonym(usePseudonym)
        }
    }

    // Notices - filtered by current user's batch
    val notices: StateFlow<List<NoticeEntity>> = profile
        .flatMapLatest { p ->
            val b = p?.batch ?: "SSC"
            repository.getNoticesForBatch(b)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNoticesForAdmin: StateFlow<List<NoticeEntity>> = repository.getAllNotices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markNoticeRead(id: String) {
        viewModelScope.launch {
            repository.markNoticeAsRead(id)
        }
    }

    fun createNotice(title: String, content: String, batch: String, category: String, isPinned: Boolean) {
        viewModelScope.launch {
            repository.createNotice(title, content, batch, category, isPinned)
        }
    }

    fun deleteNotice(notice: NoticeEntity) {
        viewModelScope.launch {
            repository.deleteNotice(notice)
        }
    }

    // Videos - filtered by batch
    val videos: StateFlow<List<VideoLessonEntity>> = profile
        .flatMapLatest { p ->
            val b = p?.batch ?: "SSC"
            repository.getVideosForBatch(b)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleVideoBookmark(id: String, current: Boolean) {
        viewModelScope.launch {
            repository.toggleVideoBookmark(id, current)
        }
    }

    fun registerVideoLesson(title: String, subject: String, batch: String, chapter: String, url: String, duration: Int) {
        viewModelScope.launch {
            repository.registerVideoLesson(title, subject, batch, chapter, url, duration)
        }
    }

    // Discussion Messages - strictly isolated by Batch
    val discussionMessages: StateFlow<List<DiscussionMessageEntity>> = profile
        .flatMapLatest { p ->
            val b = p?.batch ?: "SSC"
            repository.getDiscussionMessagesForBatch(b)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reportedMessages: StateFlow<List<DiscussionMessageEntity>> = repository.getReportedMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun postQuestion(subject: String, chapter: String, title: String, question: String) {
        val p = profile.value ?: return
        viewModelScope.launch {
            repository.postQuestion(
                batch = p.batch,
                authorName = p.name,
                authorRole = p.role,
                subject = subject,
                chapter = chapter,
                title = title,
                question = question
            )
        }
    }

    fun postReply(messageId: String, replyText: String) {
        val p = profile.value ?: return
        viewModelScope.launch {
            repository.postReply(
                messageId = messageId,
                authorName = if (p.role == "ADMIN") "আলম স্যার (ALAM Sir)" else p.name,
                authorRole = p.role,
                replyText = replyText,
                isHelpful = p.role == "ADMIN"
            )
        }
    }

    fun reportDiscussionMessage(messageId: String, reason: String) {
        viewModelScope.launch {
            repository.reportDiscussionMessage(messageId, reason)
        }
    }

    fun dismissMessageReport(messageId: String) {
        viewModelScope.launch {
            repository.dismissMessageReport(messageId)
        }
    }

    fun deleteDiscussionMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteDiscussionMessage(messageId)
        }
    }

    // Active Discussion Message replies
    private val _selectedMessageId = MutableStateFlow<String?>(null)
    val activeReplies: StateFlow<List<DiscussionReplyEntity>> = _selectedMessageId
        .filterNotNull()
        .flatMapLatest { id -> repository.getRepliesForMessage(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDiscussionMessage(message: DiscussionMessageEntity) {
        _selectedMessageId.value = message.id
        _currentScreen.value = CurrentScreen.DiscussionThread(message)
    }

    // Exams
    val exams: StateFlow<List<ExamEntity>> = profile
        .flatMapLatest { p ->
            val b = p?.batch ?: "SSC"
            repository.getExamsForBatch(b)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExamsForAdmin: StateFlow<List<ExamEntity>> = repository.getAllExams()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Exam Session State
    private var examTimerJob: Job? = null
    private val _activeExamQuestions = MutableStateFlow<List<QuestionEntity>>(emptyList())
    val activeExamQuestions: StateFlow<List<QuestionEntity>> = _activeExamQuestions.asStateFlow()

    private val _selectedAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap()) // questionIndex -> selectedOption
    val selectedAnswers: StateFlow<Map<Int, Int>> = _selectedAnswers.asStateFlow()

    private val _remainingTimeSeconds = MutableStateFlow<Int>(0)
    val remainingTimeSeconds: StateFlow<Int> = _remainingTimeSeconds.asStateFlow()

    fun startExam(exam: ExamEntity) {
        viewModelScope.launch {
            val questions = repository.getQuestionsForExam(exam.id)
            _activeExamQuestions.value = questions
            _selectedAnswers.value = emptyMap()
            _remainingTimeSeconds.value = exam.durationMinutes * 60
            _currentScreen.value = CurrentScreen.ExamTaking(exam)

            startTimer(exam)
        }
    }

    private fun startTimer(exam: ExamEntity) {
        examTimerJob?.cancel()
        examTimerJob = viewModelScope.launch {
            while (_remainingTimeSeconds.value > 0) {
                delay(1000)
                _remainingTimeSeconds.value -= 1
            }
            // Auto submit when time expires
            submitExam(exam)
        }
    }

    fun selectAnswer(questionIndex: Int, option: Int) {
        _selectedAnswers.value = _selectedAnswers.value.toMutableMap().apply {
            put(questionIndex, option)
        }
    }

    fun submitExam(exam: ExamEntity) {
        examTimerJob?.cancel()
        val p = profile.value ?: return
        val questions = _activeExamQuestions.value
        val answers = _selectedAnswers.value
        val totalSecs = (exam.durationMinutes * 60) - _remainingTimeSeconds.value

        var correctCount = 0
        var wrongCount = 0
        var unansweredCount = 0

        questions.forEachIndexed { index, q ->
            val selected = answers[index]
            if (selected == null) {
                unansweredCount++
            } else if (selected == q.correctOption) {
                correctCount++
            } else {
                wrongCount++
            }
        }

        val marksPerQuestion = if (questions.isNotEmpty()) exam.totalMarks.toFloat() / questions.size else 1f
        val rawScore = (correctCount * marksPerQuestion) - (wrongCount * exam.negativeMarkPerQuestion)
        val finalScore = maxOf(0f, rawScore)

        val jsonObj = JSONObject()
        answers.forEach { (k, v) -> jsonObj.put(k.toString(), v) }

        val studentDisplayName = if (p.isPseudonymInRank) "শিক্ষার্থী #${p.roll.takeLast(4)}" else p.name

        viewModelScope.launch {
            val attempt = repository.submitExamAttempt(
                examId = exam.id,
                studentName = studentDisplayName,
                studentRoll = p.roll,
                batch = p.batch,
                score = finalScore,
                totalMarks = exam.totalMarks,
                correctCount = correctCount,
                wrongCount = wrongCount,
                unansweredCount = unansweredCount,
                timeTakenSeconds = totalSecs,
                answersJson = jsonObj.toString()
            )
            _currentScreen.value = CurrentScreen.ExamResult(attempt, exam)
        }
    }

    fun getLeaderboard(examId: String): Flow<List<ExamAttemptEntity>> {
        return repository.getLeaderboard(examId)
    }

    // Fees & Payments (bKash/Nagad 01577654118)
    val studentInvoices: StateFlow<List<FeeInvoiceEntity>> = profile
        .flatMapLatest { p ->
            val r = p?.roll ?: "SSC-2026-042"
            repository.getInvoicesForStudent(r)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studentSubmissions: StateFlow<List<PaymentSubmissionEntity>> = profile
        .flatMapLatest { p ->
            val r = p?.roll ?: "SSC-2026-042"
            repository.getSubmissionsForStudent(r)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingSubmissionsForAdmin: StateFlow<List<PaymentSubmissionEntity>> = repository.getPendingSubmissions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubmissionsForAdmin: StateFlow<List<PaymentSubmissionEntity>> = repository.getAllSubmissions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun submitFeePayment(
        invoiceId: String,
        method: String,
        amount: Int,
        trxId: String,
        senderPhone: String
    ) {
        val p = profile.value ?: return
        viewModelScope.launch {
            repository.submitPayment(
                invoiceId = invoiceId,
                studentName = p.name,
                studentRoll = p.roll,
                batch = p.batch,
                method = method,
                amount = amount,
                trxId = trxId,
                senderPhone = senderPhone
            )
        }
    }

    fun verifyPayment(submissionId: String, invoiceId: String, adminNote: String?) {
        viewModelScope.launch {
            repository.verifyPayment(submissionId, invoiceId, adminNote)
        }
    }

    fun rejectPayment(submissionId: String, invoiceId: String, reason: String) {
        viewModelScope.launch {
            repository.rejectPayment(submissionId, invoiceId, reason)
        }
    }
}

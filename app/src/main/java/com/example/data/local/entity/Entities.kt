package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val batch: String, // "ALL", "SSC", "HSC"
    val category: String, // "URGENT", "EXAM", "CLASS", "PAYMENT", "GENERAL"
    val publishedAt: Long,
    val isPinned: Boolean = false,
    val isRead: Boolean = false
)

@Entity(tableName = "video_lessons")
data class VideoLessonEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String, // "BIOLOGY", "CHEMISTRY"
    val batch: String, // "SSC", "HSC", "ALL"
    val chapter: String,
    val youtubeVideoId: String,
    val youtubeUrl: String,
    val durationMinutes: Int,
    val publishDate: String,
    val isBookmarked: Boolean = false,
    val watchProgressPercent: Int = 0
)

@Entity(tableName = "discussion_messages")
data class DiscussionMessageEntity(
    @PrimaryKey val id: String,
    val batch: String, // "SSC", "HSC"
    val authorName: String,
    val authorRole: String, // "STUDENT", "TEACHER", "MODERATOR"
    val subject: String, // "BIOLOGY", "CHEMISTRY"
    val chapter: String,
    val title: String,
    val question: String,
    val hasImage: Boolean = false,
    val timestamp: Long,
    val isResolved: Boolean = false,
    val replyCount: Int = 0,
    val isReported: Boolean = false,
    val reportReason: String? = null
)

@Entity(tableName = "discussion_replies")
data class DiscussionReplyEntity(
    @PrimaryKey val id: String,
    val messageId: String,
    val authorName: String,
    val authorRole: String, // "STUDENT", "TEACHER", "MODERATOR"
    val replyText: String,
    val timestamp: Long,
    val isHelpful: Boolean = false
)

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String, // "BIOLOGY", "CHEMISTRY"
    val batch: String, // "SSC", "HSC"
    val chapter: String,
    val durationMinutes: Int,
    val totalMarks: Int,
    val negativeMarkPerQuestion: Float = 0.25f,
    val status: String, // "UPCOMING", "ACTIVE", "COMPLETED"
    val startTime: Long,
    val endTime: Long
)

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: String,
    val examId: String,
    val questionNumber: Int,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: Int, // 0..3
    val explanation: String
)

@Entity(tableName = "exam_attempts")
data class ExamAttemptEntity(
    @PrimaryKey val id: String,
    val examId: String,
    val studentName: String,
    val studentRoll: String,
    val batch: String,
    val score: Float,
    val totalMarks: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val unansweredCount: Int,
    val timestamp: Long,
    val timeTakenSeconds: Int,
    val selectedAnswersJson: String
)

@Entity(tableName = "fee_invoices")
data class FeeInvoiceEntity(
    @PrimaryKey val id: String,
    val studentRoll: String,
    val batch: String,
    val monthYear: String,
    val amount: Int,
    val dueDate: String,
    val status: String, // "UNPAID", "PENDING_VERIFICATION", "PAID"
    val receiptId: String? = null
)

@Entity(tableName = "payment_submissions")
data class PaymentSubmissionEntity(
    @PrimaryKey val id: String,
    val invoiceId: String,
    val studentName: String,
    val studentRoll: String,
    val batch: String,
    val method: String, // "BKASH", "NAGAD"
    val amount: Int,
    val trxId: String,
    val senderPhoneMasked: String,
    val submittedAt: Long,
    val status: String, // "PENDING", "VERIFIED", "REJECTED"
    val adminNote: String? = null,
    val receiptNumber: String? = null,
    val verifiedAt: Long? = null
)

@Entity(tableName = "student_profile")
data class StudentProfileEntity(
    @PrimaryKey val id: String = "current_user",
    val name: String,
    val phone: String,
    val roll: String,
    val batch: String, // "SSC", "HSC"
    val role: String = "STUDENT", // "STUDENT", "ADMIN"
    val isPseudonymInRank: Boolean = false,
    val avatarColorHex: String = "#1565C0"
)

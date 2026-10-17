package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        NoticeEntity::class,
        VideoLessonEntity::class,
        DiscussionMessageEntity::class,
        DiscussionReplyEntity::class,
        ExamEntity::class,
        QuestionEntity::class,
        ExamAttemptEntity::class,
        FeeInvoiceEntity::class,
        PaymentSubmissionEntity::class,
        StudentProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AlamsDatabase : RoomDatabase() {
    abstract fun noticeDao(): NoticeDao
    abstract fun videoLessonDao(): VideoLessonDao
    abstract fun discussionDao(): DiscussionDao
    abstract fun examDao(): ExamDao
    abstract fun feeDao(): FeeDao
    abstract fun profileDao(): ProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AlamsDatabase? = null

        fun getDatabase(context: Context): AlamsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlamsDatabase::class.java,
                    "alams_batch_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseSeederCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseSeederCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        seedInitialData(database)
                    }
                }
            }
        }

        private suspend fun seedInitialData(database: AlamsDatabase) {
            val now = System.currentTimeMillis()
            val dayMillis = 86400000L

            // 1. Student Profile
            database.profileDao().insertProfile(
                StudentProfileEntity(
                    id = "current_user",
                    name = "তানভীর হোসেন (Tanvir)",
                    phone = "01712345678",
                    roll = "SSC-2026-042",
                    batch = "SSC",
                    role = "STUDENT",
                    isPseudonymInRank = false,
                    avatarColorHex = "#1565C0"
                )
            )

            // 2. Notices
            database.noticeDao().insertNotices(
                listOf(
                    NoticeEntity(
                        id = "not-01",
                        title = "জরুরি: SSC স্পেশাল মডেল টেস্ট-১ এর সময়সূচি ঘোষণা",
                        content = "আগামীকাল রাত ৮:০০ টায় জীববিজ্ঞান ২য় অধ্যায় (জীবকোষ ও টিস্যু) এবং রসায়ন ৪র্থ অধ্যায় (পর্যায় সারণি) এর ওপর ৩০ নম্বরের পূর্ণাঙ্গ অনলাইন এমসিকিউ পরীক্ষা অনুষ্ঠিত হবে। পরীক্ষায় নেগেটিভ মার্কিং থাকবে। সকলে সঠিক সময়ে উপস্থিত থাকবে।",
                        batch = "SSC",
                        category = "URGENT",
                        publishedAt = now - 3600000,
                        isPinned = true,
                        isRead = false
                    ),
                    NoticeEntity(
                        id = "not-02",
                        title = "HSC: জৈব রসায়ন (Organic Chemistry) স্পেশাল রিভিশন ক্লাস",
                        content = "হাইড্রোকার্বন এবং বেনজিন বলয়ের বিক্রিয়া কৌশল নিয়ে বিশেষ আলোচনা ও সলভ ক্লাস YouTube চ্যানেলে উন্মুক্ত করা হয়েছে। কোনো সংশয় থাকলে সমাধান বক্সে পোস্ট করবে।",
                        batch = "HSC",
                        category = "CLASS",
                        publishedAt = now - 7200000,
                        isPinned = true,
                        isRead = false
                    ),
                    NoticeEntity(
                        id = "not-03",
                        title = "অক্টোবর ২০২৬ মাসের মাসিক বেতন পরিশোধের নির্দেশনা",
                        content = "সম্মানিত অভিভাবক ও প্রিয় শিক্ষার্থীদের জানানো যাচ্ছে যে, অক্টোবর মাসের টিউশন ফি নির্ধারিত বিকাশ/নগদ নম্বর 01577654118 (Personal)-এ পাঠিয়ে অ্যাপের ফি অপশনে TrxID জমা দেওয়ার জন্য অনুরোধ করা হলো। অ্যাডমিন ভেরিফিকেশনের পর রশিদ দেখতে পাবেন।",
                        batch = "ALL",
                        category = "PAYMENT",
                        publishedAt = now - 14400000,
                        isPinned = false,
                        isRead = true
                    ),
                    NoticeEntity(
                        id = "not-04",
                        title = "বিজ্ঞান মেলা ও ল্যাবরেটরি প্র্যাকটিক্যাল নোটিশ",
                        content = "রসায়ন ল্যাবে টাইট্রেশন এবং জীববিজ্ঞানে মাইটোসিস কোষ বিভাজন পর্যবেক্ষণের হ্যান্ডস-অন সেশন আগামী শুক্রবার সকাল ৯:০০ টায় অনুষ্ঠিত হবে।",
                        batch = "ALL",
                        category = "GENERAL",
                        publishedAt = now - 28800000,
                        isPinned = false,
                        isRead = true
                    )
                )
            )

            // 3. YouTube Class Videos (@alamssciencelab)
            database.videoLessonDao().insertVideos(
                listOf(
                    VideoLessonEntity(
                        id = "vid-01",
                        title = "জীববিজ্ঞান: কোষের গঠন ও অঙ্গাণু পরিচিতি (Cell Structure)",
                        subject = "BIOLOGY",
                        batch = "SSC",
                        chapter = "অধ্যায় ২: জীবকোষ ও টিস্যু",
                        youtubeVideoId = "g97k4X9Lq38",
                        youtubeUrl = "https://youtube.com/@alamssciencelab",
                        durationMinutes = 42,
                        publishDate = "১৬ অক্টোবর ২০২৬",
                        isBookmarked = true,
                        watchProgressPercent = 85
                    ),
                    VideoLessonEntity(
                        id = "vid-02",
                        title = "রসায়ন: পর্যায় সারণির মৌল মনে রাখার সহজ ট্রিকস ও আয়নিকরণ শক্তি",
                        subject = "CHEMISTRY",
                        batch = "SSC",
                        chapter = "অধ্যায় ৪: পর্যায় সারণি",
                        youtubeVideoId = "M7lc1UVf-VE",
                        youtubeUrl = "https://youtube.com/@alamssciencelab",
                        durationMinutes = 35,
                        publishDate = "১৪ অক্টোবর ২০২৬",
                        isBookmarked = false,
                        watchProgressPercent = 40
                    ),
                    VideoLessonEntity(
                        id = "vid-03",
                        title = "HSC জীববিজ্ঞান: DNA রেপ্লিকেশন ও সেন্ট্রাল ডগমা মেকানিজম",
                        subject = "BIOLOGY",
                        batch = "HSC",
                        chapter = "অধ্যায় ১: কোষ ও এর গঠন",
                        youtubeVideoId = "dQw4w9WgXcQ",
                        youtubeUrl = "https://youtube.com/@alamssciencelab",
                        durationMinutes = 55,
                        publishDate = "১৫ অক্টোবর ২০২৬",
                        isBookmarked = true,
                        watchProgressPercent = 0
                    ),
                    VideoLessonEntity(
                        id = "vid-04",
                        title = "HSC রসায়ন: মারকনিকভ নিয়ম ও অ্যালকিনের ইলেকট্রোফিলিক সংযোজন",
                        subject = "CHEMISTRY",
                        batch = "HSC",
                        chapter = "অধ্যায় ২: জৈব রসায়ন",
                        youtubeVideoId = "L_LUpnjgPso",
                        youtubeUrl = "https://youtube.com/@alamssciencelab",
                        durationMinutes = 48,
                        publishDate = "১১ অক্টোবর ২০২৬",
                        isBookmarked = false,
                        watchProgressPercent = 60
                    )
                )
            )

            // 4. Discussion Forum (Problem Solving Box)
            database.discussionDao().insertMessages(
                listOf(
                    DiscussionMessageEntity(
                        id = "msg-01",
                        batch = "SSC",
                        authorName = "সাদিয়া ইসলাম",
                        authorRole = "STUDENT",
                        subject = "CHEMISTRY",
                        chapter = "পর্যায় সারণি",
                        title = "হ্যালোজেন মৌলগুলোর সক্রিয়তার ক্রম কীভাবে মনে রাখব?",
                        question = "ফ্লোরিন, ক্লোরিন, ব্রোমিন ও আয়োডিনের মধ্যে কেন ফ্লোরিন সবচেয়ে তীব্র জারক এবং সহজে ইলেকট্রন গ্রহণ করে? কারো কাছে কি শর্টকাট নোট আছে?",
                        hasImage = false,
                        timestamp = now - 1800000,
                        isResolved = true,
                        replyCount = 2
                    ),
                    DiscussionMessageEntity(
                        id = "msg-02",
                        batch = "SSC",
                        authorName = "আরিফুর রহমান",
                        authorRole = "STUDENT",
                        subject = "BIOLOGY",
                        chapter = "জীবকোষ ও টিস্যু",
                        title = "মাইটোকন্ড্রিয়াকে কেন পাওয়ার হাউস বলা হয়?",
                        question = "ক্রেবস চক্র এবং এটিপি সিন্থেসিস কোন অংশে সংঘটিত হয়? ক্রিস্টি নাকি ম্যাট্রিক্সে?",
                        hasImage = false,
                        timestamp = now - 7200000,
                        isResolved = true,
                        replyCount = 1
                    ),
                    DiscussionMessageEntity(
                        id = "msg-03",
                        batch = "HSC",
                        authorName = "মেহজাবিন চৌধুরী",
                        authorRole = "STUDENT",
                        subject = "CHEMISTRY",
                        chapter = "জৈব রসায়ন",
                        title = "কার্বোক্যাটায়নের স্থায়িত্বের ক্রম 3° > 2° > 1° হওয়ার মূল কারণ কী?",
                        question = "হাইপারকনজুগেশন নাকি ইনডাক্টিভ ইফেক্ট—কোনটি প্রধান ভূমিকা পালন করে?",
                        hasImage = false,
                        timestamp = now - 3600000,
                        isResolved = false,
                        replyCount = 1
                    )
                )
            )

            database.discussionDao().insertReplies(
                listOf(
                    DiscussionReplyEntity(
                        id = "rep-01",
                        messageId = "msg-01",
                        authorName = "আলম স্যার (ALAM Sir)",
                        authorRole = "TEACHER",
                        replyText = "চমৎকার প্রশ্ন! ফ্লোরিনের পরমাণুর আকার ক্ষুদ্র হওয়ায় এর নিউক্লিয়াসের আকর্ষণ সবচেয়ে বেশি। ফলে এটি দ্রুত বাইরে থেকে ইলেকট্রন টেনে তীব্র জারণ ক্ষমতা দেখায়। সক্রিয়তার ক্রম: F2 > Cl2 > Br2 > I2।",
                        timestamp = now - 1200000,
                        isHelpful = true
                    ),
                    DiscussionReplyEntity(
                        id = "rep-02",
                        messageId = "msg-01",
                        authorName = "রাফি আহমেদ",
                        authorRole = "STUDENT",
                        replyText = "ধন্যবাদ স্যার! ক্লাসে যে পর্যায় সারণির ছক দিয়েছিলেন তা দেখেও মিলিয়ে নিলাম।",
                        timestamp = now - 600000,
                        isHelpful = false
                    ),
                    DiscussionReplyEntity(
                        id = "rep-03",
                        messageId = "msg-02",
                        authorName = "তানভীর হোসেন",
                        authorRole = "STUDENT",
                        replyText = "ক্রেবস চক্র মাইটোকন্ড্রিয়ার ম্যাট্রিক্সে হয় এবং ইলেকট্রন ট্রান্সপোর্ট সিস্টেম (ETS) ভেতরের ঝিল্লি ও ক্রিস্টিতে ঘটে। মোট ৩৮টি (বা ৩২টি আধুনিক মতে) এটিপির সিংহভাগ এখানেই তৈরি হয়।",
                        timestamp = now - 3000000,
                        isHelpful = true
                    ),
                    DiscussionReplyEntity(
                        id = "rep-04",
                        messageId = "msg-03",
                        authorName = "আলম স্যার (ALAM Sir)",
                        authorRole = "TEACHER",
                        replyText = "উভয় প্রভাব কাজ করে, তবে আলফা-হাইড্রোজেনের হাইপারকনজুগেশন এখানে প্রধান প্রভাবক। ৩° ক্যাটায়নে ৯টি আলফা-হাইড্রোজেন রেজোন্যান্স সৃষ্টি করে সর্বাধিক চার্জ বিস্তার করে।",
                        timestamp = now - 1800000,
                        isHelpful = true
                    )
                )
            )

            // 5. Exams & Questions
            val exam1Id = "exam-ssc-01"
            val exam2Id = "exam-hsc-01"

            database.examDao().insertExams(
                listOf(
                    ExamEntity(
                        id = exam1Id,
                        title = "SSC মেগা টেস্ট: জীববিজ্ঞান ও রসায়ন সমন্বিত পরীক্ষা",
                        subject = "BIOLOGY",
                        batch = "SSC",
                        chapter = "কোষ ও পর্যায় সারণি",
                        durationMinutes = 15,
                        totalMarks = 20,
                        negativeMarkPerQuestion = 0.25f,
                        status = "ACTIVE",
                        startTime = now - 3600000,
                        endTime = now + 86400000
                    ),
                    ExamEntity(
                        id = exam2Id,
                        title = "HSC স্পেশাল মক: জৈব রসায়ন ও জেনেটিক্স",
                        subject = "CHEMISTRY",
                        batch = "HSC",
                        chapter = "জৈব রসায়ন ১ম পত্র",
                        durationMinutes = 20,
                        totalMarks = 25,
                        negativeMarkPerQuestion = 0.25f,
                        status = "ACTIVE",
                        startTime = now - 3600000,
                        endTime = now + 86400000
                    )
                )
            )

            // Questions for Exam 1 (SSC)
            database.examDao().insertQuestions(
                listOf(
                    QuestionEntity(
                        id = "q-01",
                        examId = exam1Id,
                        questionNumber = 1,
                        questionText = "উদ্ভিদকোষের প্লাস্টিডের কোন বর্ণকণিকা সালোকসংশ্লেষণে মুখ্য ভূমিকা পালন করে?",
                        optionA = "ক্লোরোফিল (Chlorophyll)",
                        optionB = "ক্যারোটিনয়েড (Carotenoid)",
                        optionC = "লিউ কোপ্লাস্ট (Leucoplast)",
                        optionD = "অ্যান্থোসায়ানিন (Anthocyanin)",
                        correctOption = 0,
                        explanation = "ক্লোরোফিল সূর্যের আলো শোষণ করে আলোক শক্তিকে রাসায়নিক শক্তিতে রূপান্তরিত করে।"
                    ),
                    QuestionEntity(
                        id = "q-02",
                        examId = exam1Id,
                        questionNumber = 2,
                        questionText = "পর্যায় সারণির কোন গ্রুপের মৌলগুলোকে ক্ষার ধাতু (Alkali Metals) বলা হয়?",
                        optionA = "গ্রুপ ২",
                        optionB = "গ্রুপ ১",
                        optionC = "গ্রুপ ১৭",
                        optionD = "গ্রুপ ১৮",
                        correctOption = 1,
                        explanation = "গ্রুপ ১-এর মৌলসমূহ (Li, Na, K, Rb, Cs, Fr) পানির সাথে বিক্রিয়া করে তীব্র ক্ষার উৎপন্ন করে।"
                    ),
                    QuestionEntity(
                        id = "q-03",
                        examId = exam1Id,
                        questionNumber = 3,
                        questionText = "মানবদেহে আমিষ পরিপাকের প্রধান এনজাইম পেপসিন কোথায় সক্রিয় হয়?",
                        optionA = "মুখগহ্বরে",
                        optionB = "পাকস্থলীতে (Stomach)",
                        optionC = "ক্ষুদ্রান্ত্রে",
                        optionD = "যকৃতে",
                        correctOption = 1,
                        explanation = "পাকস্থলীর গ্যাস্ট্রিক রসে উপস্থিত HCl এর উপস্থিতিতে পেপসিনোজেন পেপসিনে সক্রিয় হয়ে আমিষ পরিপাক শুরু করে।"
                    ),
                    QuestionEntity(
                        id = "q-04",
                        examId = exam1Id,
                        questionNumber = 4,
                        questionText = "নিচের কোন যৌগে সমযোজী ও আয়নিক উভয় বন্ধন বিদ্যমান?",
                        optionA = "NaCl",
                        optionB = "NH4Cl (অ্যামোনিয়াম ক্লোরাইড)",
                        optionC = "CH4",
                        optionD = "H2O",
                        correctOption = 1,
                        explanation = "NH4+ আয়নের মধ্যে সমযোজী ও সন্নিবেশ বন্ধন এবং NH4+ ও Cl- এর মধ্যে আয়নিক বন্ধন থাকে।"
                    ),
                    QuestionEntity(
                        id = "q-05",
                        examId = exam1Id,
                        questionNumber = 5,
                        questionText = "কোষের প্রোটিন তৈরির কারখানা বলা হয় কোন অঙ্গাণুকে?",
                        optionA = "গলগি বডি",
                        optionB = "রাইবোসোম (Ribosome)",
                        optionC = "লাইসোসোম",
                        optionD = "সেন্ট্রিওল",
                        correctOption = 1,
                        explanation = "রাইবোসোমে ট্রান্সলেশন প্রক্রিয়ায় বিভিন্ন অ্যামাইনো এসিড যুক্ত হয়ে প্রোটিন শৃঙ্খল সংশ্লেষিত হয়।"
                    )
                )
            )

            // Seed some realistic batch leaderboard attempts
            database.examDao().insertAttempts(
                listOf(
                    ExamAttemptEntity(
                        id = "att-01",
                        examId = exam1Id,
                        studentName = "সায়মা জাহান",
                        studentRoll = "SSC-2026-003",
                        batch = "SSC",
                        score = 20.0f,
                        totalMarks = 20,
                        correctCount = 5,
                        wrongCount = 0,
                        unansweredCount = 0,
                        timestamp = now - 1800000,
                        timeTakenSeconds = 410,
                        selectedAnswersJson = "{\"0\":0,\"1\":1,\"2\":1,\"3\":1,\"4\":1}"
                    ),
                    ExamAttemptEntity(
                        id = "att-02",
                        examId = exam1Id,
                        studentName = "রাকিবুল হাসান",
                        studentRoll = "SSC-2026-015",
                        batch = "SSC",
                        score = 15.75f,
                        totalMarks = 20,
                        correctCount = 4,
                        wrongCount = 1,
                        unansweredCount = 0,
                        timestamp = now - 2400000,
                        timeTakenSeconds = 520,
                        selectedAnswersJson = "{\"0\":0,\"1\":1,\"2\":1,\"3\":0,\"4\":1}"
                    ),
                    ExamAttemptEntity(
                        id = "att-03",
                        examId = exam1Id,
                        studentName = "ফারহানা আক্তার",
                        studentRoll = "SSC-2026-029",
                        batch = "SSC",
                        score = 15.0f,
                        totalMarks = 20,
                        correctCount = 4,
                        wrongCount = 0,
                        unansweredCount = 1,
                        timestamp = now - 3000000,
                        timeTakenSeconds = 480,
                        selectedAnswersJson = "{\"0\":0,\"1\":1,\"2\":1,\"4\":1}"
                    )
                )
            )

            // 6. Fee Invoices and Payments
            val inv1 = "inv-oct-2026"
            val inv2 = "inv-sep-2026"

            database.feeDao().insertInvoices(
                listOf(
                    FeeInvoiceEntity(
                        id = inv1,
                        studentRoll = "SSC-2026-042",
                        batch = "SSC",
                        monthYear = "অক্টোবর ২০২৬",
                        amount = 1500,
                        dueDate = "২৫ অক্টোবর ২০২৬",
                        status = "UNPAID",
                        receiptId = null
                    ),
                    FeeInvoiceEntity(
                        id = inv2,
                        studentRoll = "SSC-2026-042",
                        batch = "SSC",
                        monthYear = "সেপ্টেম্বর ২০২৬",
                        amount = 1500,
                        dueDate = "২৫ সেপ্টেম্বর ২০২৬",
                        status = "PAID",
                        receiptId = "ALAM-REC-2026-0941"
                    )
                )
            )

            database.feeDao().insertSubmissions(
                listOf(
                    PaymentSubmissionEntity(
                        id = "sub-sep-01",
                        invoiceId = inv2,
                        studentName = "তানভীর হোসেন",
                        studentRoll = "SSC-2026-042",
                        batch = "SSC",
                        method = "BKASH",
                        amount = 1500,
                        trxId = "BL92M8P71Q",
                        senderPhoneMasked = "017***5678",
                        submittedAt = now - 25 * dayMillis,
                        status = "VERIFIED",
                        adminNote = "অফিসিয়াল bKash ট্রান্সফার ভেরিফাইড ও গৃহীত।",
                        receiptNumber = "ALAM-REC-2026-0941",
                        verifiedAt = now - 24 * dayMillis
                    ),
                    // Sample pending submission from another student for Admin review queue
                    PaymentSubmissionEntity(
                        id = "sub-oct-pending-01",
                        invoiceId = "inv-other-01",
                        studentName = "নুসরাত ফারিয়া",
                        studentRoll = "SSC-2026-018",
                        batch = "SSC",
                        method = "NAGAD",
                        amount = 1500,
                        trxId = "NG884K10P2",
                        senderPhoneMasked = "018***4433",
                        submittedAt = now - 7200000,
                        status = "PENDING",
                        adminNote = null,
                        receiptNumber = null,
                        verifiedAt = null
                    )
                )
            )
        }
    }
}

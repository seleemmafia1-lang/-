package com.example.data

import com.example.data.model.QualityProject
import com.example.data.model.QualityRule
import com.example.data.model.RaneenUser
import com.example.data.model.UserRole

object DefaultData {
    val INITIAL_USERS: List<RaneenUser> = listOf(
        RaneenUser(
            id = "user-inspector-1",
            username = "inspector",
            passwordHash = "123",
            role = UserRole.INSPECTOR,
            fullName = "م. أحمد محمود",
            branchName = "فرع رنين - الهرم"
        ),
        RaneenUser(
            id = "user-manager-1",
            username = "manager",
            passwordHash = "123",
            role = UserRole.QUALITY_MANAGER,
            fullName = "د. خالد السعيد",
            branchName = "الإدارة العامة للجودة"
        ),
        RaneenUser(
            id = "user-branch-1",
            username = "branch",
            passwordHash = "123",
            role = UserRole.BRANCH_MANAGER,
            fullName = "أ. طارق عبد الرحمن",
            branchName = "فرع رنين - فيصل"
        ),
        RaneenUser(
            id = "user-admin-1",
            username = "admin",
            passwordHash = "123",
            role = UserRole.ADMIN,
            fullName = "م. سامح إبراهيم",
            branchName = "المقر الرئيسي"
        )
    )

    val INITIAL_PROJECTS: List<QualityProject> = listOf(
        QualityProject(
            id = 1,
            name = "فرع رنين - الهرم",
            branchCode = "RN-HRM-01",
            city = "الجيزة",
            managerName = "أ. حسام فؤاد",
            description = "تقييم الجودة ومعايير العرض التجاري لفرع الهرم الرئيسي",
            targetScore = 92
        ),
        QualityProject(
            id = 2,
            name = "فرع رنين - فيصل",
            branchCode = "RN-FSL-02",
            city = "الجيزة",
            managerName = "أ. طارق عبد الرحمن",
            description = "متابعة السلامة المهنية ومستوى النظافة والتسعير",
            targetScore = 90
        ),
        QualityProject(
            id = 3,
            name = "فرع رنين - مدينة نصر (مكرم عبيد)",
            branchCode = "RN-NASR-03",
            city = "القاهرة",
            managerName = "م. شريف عادل",
            description = "التدقيق الدوري الشامل على الأقسام والأجهزة الكهربائية والأدوات المنزلية",
            targetScore = 95
        ),
        QualityProject(
            id = 4,
            name = "فرع رنين - المهندسين",
            branchCode = "RN-MOH-04",
            city = "الجيزة",
            managerName = "أ. إيهاب كمال",
            description = "مراقبة جودة الخدمة وتنسيق الرفوف والمخازن",
            targetScore = 90
        )
    )

    val INITIAL_RULES: List<QualityRule> = listOf(
        QualityRule("Q-001", "تقييم مستوى النظافة", "نظافة الرفوف وأسطح الطاولات", false, 1),
        QualityRule("Q-002", "تقييم مستوى النظافة", "المنتجات نظيفة", false, 2),
        QualityRule("Q-003", "تقييم مستوى النظافة", "عدم وجود روائح كريهة", false, 3),
        QualityRule("Q-004", "تقييم مستوى النظافة", "الأرضيات والأسقف والحوائط نظيفة", false, 4),
        QualityRule("Q-005", "تقييم مستوى النظافة", "نظافة العرض ووسائل العرض وأرفف الصالة", false, 5),
        QualityRule("Q-006", "تقييم مستوى النظافة", "نظافة وترتيب المخزن", false, 6),
        QualityRule("Q-007", "تنسيق العرض", "تنسيق المنتجات على الرفوف والبوديومات", false, 7),
        QualityRule("Q-008", "تنسيق العرض", "تنسيق وترتيب العروض الأرضية", false, 8),
        QualityRule("Q-009", "تنسيق العرض", "تنسيق وترتيب المنتجات داخل مساحات العرض", false, 9),
        QualityRule("Q-010", "تنسيق العرض", "إعادة المنتجات المردودة إلى أماكنها", false, 10),
        QualityRule("Q-011", "تنسيق العرض", "عرض المنتجات طبقًا لسياسة العرض", false, 11),
        QualityRule("Q-012", "التسعير والتكويد", "تسعير المنتجات طبقًا لطرق التسعير المعتمدة", false, 12),
        QualityRule("Q-013", "التسعير والتكويد", "جميع المنتجات مكوّدة بكود صحيح", false, 13),
        QualityRule("Q-014", "التسعير والتكويد", "ملصقات الأسعار موجودة وواضحة وغير تالفة", false, 14),
        QualityRule("Q-015", "التسعير والتكويد", "العبوات والصناديق معروضة بطريقة صحيحة", false, 15),
        QualityRule("Q-016", "سلامة المنتجات وأدوات العرض", "وضع المنتجات بطريقة تمنع التلف أو السرقة", false, 16),
        QualityRule("Q-017", "سلامة المنتجات وأدوات العرض", "العروض الأرضية مرفوعة على قواعد مناسبة", false, 17),
        QualityRule("Q-018", "سلامة المنتجات وأدوات العرض", "سلامة المنتج وسلامة الرفوف", false, 18),
        QualityRule("Q-019", "سلامة المنتجات وأدوات العرض", "سهولة الحركة وخلو الممرات من المعوقات", false, 19),
        QualityRule("Q-020", "سلامة المنتجات وأدوات العرض", "سلامة أجهزة العمل وأدوات العرض", false, 20),
        QualityRule("Q-021", "مستوى الخدمة", "نظافة الموظفين والالتزام بالعمل", false, 21),
        QualityRule("Q-022", "مستوى الخدمة", "التعامل اللائق مع العملاء وتقديم المساعدة", false, 22),
        QualityRule("Q-023", "مستوى الخدمة", "المنتجات المتاحة معروضة بالصالة", false, 23),
        QualityRule("Q-024", "تقييم فريق العمل", "زي العمل ومظهر وهندام الفريق", false, 24),
        QualityRule("Q-025", "تقييم فريق العمل", "الالتزام الفني للقسم والالتزام بالعرض", false, 25)
    )
}

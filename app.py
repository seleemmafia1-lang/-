import streamlit as st

st.set_page_config(page_title="نظام الرقابة والجودة - شركة رنين", layout="wide")

# تسجيل الدخول
st.sidebar.title("🔐 تسجيل الدخول")
role = st.sidebar.selectbox("اختر نوع الحساب (للتجربة):", [
    "مراقب جودة (Auditor)", 
    "مدير منطقة (Area Manager)", 
    "مدير الرقابة والجودة (Quality Director)", 
    "مدير النظام (Admin)"
])

st.title("🏢 شركة رنين - نظام إدارة الرقابة والجودة للمعارض")

# 1. شاشة مراقب الجودة
if role == "مراقب جودة (Auditor)":
    st.header("📝 نموذج تقييم معرض ميداني")
    col1, col2 = st.columns(2)
    with col1:
        st.selectbox("اختر المعرض / الفرع:", ["فرع العبور 1", "فرع مدينة نصر", "فرع الجيزة"])
    with col2:
        st.text_input("اسم المراقب:", "أحمد محمود")

    st.subheader("جدول تقييم الأقسام والضوابط")
    
    categories = ["لعب الأطفال", "وسائل تعليمية", "أثاث ومستلزمات أطفال", "الأحذية", "الحقائب"]
    rules = [
        {"id": 1, "name": "خلفيات الرفوف والممر نظيفة ومترتبة", "weight": 4},
        {"id": 2, "name": "استكمال كروت الأسعار وتطابقها مع الصنف", "weight": 4},
        {"id": 3, "name": "عدم وجود أرفف/ممرات مكسورة أو متهالكة", "weight": 4},
        {"id": 4, "name": "نظافة وتنسيق العروض الخاصة داخل القسم", "weight": 20},
        {"id": 5, "name": "سلامة المنتجات من التلف والاتساخ والكسر", "weight": 16},
    ]

    for rule in rules:
        st.write(f"**ضابط ({rule['id']}): {rule['name']} (الوزن: {rule['weight']})**")
        cols = st.columns(len(categories) + 1)
        violations = 0
        for idx, cat in enumerate(categories):
            with cols[idx]:
                checked = st.checkbox(f"{cat}", key=f"r_{rule['id']}_c_{idx}")
                if checked:
                    violations += 1
        with cols[-1]:
            st.error(f"المخالفات: {violations}")
        
        # 📸 إرفاق صور متعددة للمخالفة الواحدة
        uploaded_files = st.file_uploader(
            f"📸 إرفاق صور المخالفة لـ ({rule['name']}) - يمكنك تحديد أكثر من صورة", 
            type=["jpg", "jpeg", "png"], 
            accept_multiple_files=True,
            key=f"img_{rule['id']}"
        )
        
        # معاينة الصور المرفقة
        if uploaded_files:
            st.write(f"🖼️ عدد الصور المرفقة: **{len(uploaded_files)}**")
            img_cols = st.columns(min(len(uploaded_files), 4))
            for i, file in enumerate(uploaded_files):
                with img_cols[i % 4]:
                    st.image(file, use_container_width=True, caption=f"صورة {i+1}")
                    
        st.markdown("---")
    
    if st.button("📤 حفظ وإرسال التقرير لمدير المنطقة"):
        st.success("تم حفظ التقرير وإرساله لمدير المنطقة بنجاح!")

# 2. شاشة مدير المنطقة
elif role == "مدير منطقة (Area Manager)":
    st.header("🔍 مراجعة تقارير المنطقة")
    st.info("التقارير بانتظار الاعتماد والمراجعة:")
    st.write("• تقرير فرع العبور 1 - المراقب: أحمد محمود (يوجد 7 مخالفات ومرفق بها صور)")
    st.text_area("إضافة ملاحظات مدير المنطقة:")
    if st.button("✅ اعتماد التقرير وإرساله لمدير الرقابة والجودة"):
        st.success("تم تحويل التقرير لمدير الرقابة والجودة.")

# 3. شاشة مدير الرقابة والجودة / Admin
else:
    st.header("⚙️ لوحة تحكم إدارة الضوابط والأقسام")
    col_a, col_b = st.columns(2)
    with col_a:
        st.subheader("➕ إضافة ضابط جديد")
        new_rule = st.text_input("اسم الضابط الجديد:")
        weight = st.number_input("وزن الضابط:", min_value=1, value=4)
        if st.button("إضافة الضابط (يظهر فوراً للمراقب)"):
            st.success(f"تمت إضافة الضابط '{new_rule}' وربطه بنماذج التقييم فوريًا!")
            
    with col_b:
        st.subheader("➕ إضافة قسم / Category جديد")
        new_cat = st.text_input("اسم الكاتجري الجديد:")
        if st.button("إضافة الكاتجري"):
            st.success(f"تمت إضافة الكاتجري '{new_cat}' للجدول الرئيسي!")

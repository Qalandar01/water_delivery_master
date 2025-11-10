package uz.pdp.water_delivery.bot;

public interface BotConstant {

    String START = "/start";
    String PLEASE_SHARE_CONTACT = "☎️ Iltimos botdan to'liq foydalanish uchun kontakt yuborish tugmasini bosing !!! 👇";
    String PLEASE_SHARE_LOCATION = "📍 Qaysi manzilga buyurtma qilmoqchisiz ?";
    String SHARE_LOCATION = "📍 Locatsiyani yuborish";
    String SHARE_CONTACT = "☎️ Kontakt yuborish";
    String DONE_ALREADY = "⏳ Sizga tez orada aloqaga chiqamiz. Iltimos kutib turing...!";
    String ORDER_BTN = "🛒 Suv buyurtma berish";
    String START_ORDERING = "START_ORDERING";
    String PLUS = "+";
    String MINUS = "-";
    String CONFIRM_BTN = "✅ Tasdiqlash";
    String CONFIRM_ORDER = "CONFIRM_ORDER";
    String ORDER_FINISH_MSG = "✅ Buyurtmangiz qabul qilindi va 1-2 ish kuni ichida yetkazib beriladi. Tanlovingiz uchun raxmat! \n"  +
            "☎️ Call Center: +998 95 600 44 41";
    String ORDER_INFO = """
                            <b>🔖 Sizning buyurtmangiz:</b>
                            <b>🧮 %s</b>  dan <b>%d</b> ta
                            <b>💰 Jami:</b> %s sum
                            ⏱️ Yetkazish vaqti
                            🌗 Kun: %s
                            🕙 Vaqt: %s
            """;
    String CANCEL = "🚫 Bekor qilish";
    String REGION = "region_";
    String PLEASE_CHOOSE_REGION = "📍 Iltimos viloyatingizni tanlang:";
    String BOTTLE_TYPE = "bottleType_";
    String CANCEL_BTN = "🚫 Bekor qilish";
    String PASSWORD = "🔢 Parolni kiriting";
    String NEW_PASSWORD = "🔢 Sizda parol mavjud emas. Iltimos Yangi parolni kiriting";
    String CONFIRM_PASSWORD = "🔢 Parolni tasdiqlash uchun yangi parolni qayta kiriting";
    String START_DELIVERY = "🚚 Buyurtmalarim boshlash";
    String PLEASE_ENTER_PASSWORD = "🔢 Iltimos parolni kiriting";
    String INCORRECT_PASSWORD = " 🚫 Parol xato. Iltimos parolni kiriting";
    String CHOSE_DELIVERY_TIME = "🕐 Vaqtni tanlang";
    String START_DELIVERY_MESSAGE = "👏 Assalomu alaykum. Buyurtmalarni boshlash tugmasini bosing !!!";
    String USER_DID_NOT_ANSWER = "🚫 Siz bilan bog'lanishda xatolik yuz berdi. Iltimos (+998 95 600 44 41) raqamiga qo'ng'iroq qiling!!! ";
    String BACK = "⏮️ Orqaga";
    String START_DELIVERED = " ✅ Yetkazishni boshlash";
    String NEXT = "Keyingisi ⏭️";
    String START_DELIVERED_ORDER = "✅ Telefonga javob berdi";
    String PHONE_OFF = "🚫 Telefonga javob bermadi";
    String ARRIVED = " ✅ Yetkazildi";
    String PAYMENT_DONE = " ✅ To'lov qilindi";

    String SETTING = "⚙️ Sozlamalar";
    String NEW_LOCATION = "📍 Manzilni o'zgartirish";
    String CABINET = "🏠 Menuga qaytish";
    String LOCATION = "📍 Manzilni yuboring";
    String LOCATION_SAVED = "✅ Manzil muvaffaqiyatli o'zgartirildi. Operatorlarimiz tez orada sizga qo'ng'riq qilishadi.";
    String NEW_LOCATION_MESSAGE = "📍 Iltimos manzilingizni kiriting";
    String MY_ORDERS = "🛒 Buyurtmalarim";
    String HAS_ORDER = "📵 Sizning buyurtmangiz yakunlanmagan. Iltimos buyurtmangizni yetkazib berishlarini kuting !!! Savollaringiz bo'lsa qo'ng'iroq qilishingiz mumkin." +
            " \n\n☎️ Call Center: +998 95 600 44 41\n\n";
    String DONE = "✅ So'rovingiz qabul qilindi. Tez orada Operatorlarimiz siz bilan bog'lanishadi.";
    String NO_ORDERS = "🚫 Buyurtmalarim yo'q";
    String MENU = "🏠 Menu:";
    String PLEASE_WAITING_OPERATOR = "🙋‍♂️ Iltimos kuting. Buyurtmangizni vaqtida yetkazishimizda manzilingizda muammo bo'lmasligi uchun Operatorlarimiz siz bilan bog'lanishadi.";
    String BASKET = "🛒 Savatcha";
    String SELECT_BOTTLE_TYPE = " 📦 Mahsulot tanlang";
    String ADD_TO_BASKET = " 🛒 Savatga qo'shish";
    String REMOVE_FROM_BASKET = " 🛒 Savatdan o'chirish";
    String BASKET_EMPTY = "🚫 Savatcha bo'sh";
    String OLD_PASSWORD = "🔢 Sizda parol mavjud emas. Iltimos Yangi parolni kiriting";
    String NO_BASKET =  "🚫 Savatcha bo'sh";
    String BASKET_MESSAGE = " 🛒 Savatcha: \n";
    String SUPPORT = " 📞 Call Center: +998 95 600 44 41";
    String CREATE_ORDER = "✅ Buyurtma yuborish";
    String CLEAR_ORDER = "🚫 Buyurtmani tozalash";
    String DELETE = "🚫 Bekor qilish";
    String NO_BOTTLE_TYPE = "❌ Suv mavjud emas";
    String NO_PHONE_CONNECTION = "🚫 Xurmatli mijoz. Buyurtmangizni yetkazish uchun siz bilan bog'lanib bo'lmadi. Iltimos +998 71 200 00 00 ga murojaat qiling.";
    String START_DELIVERING_ORDER = "🚛 Xurmatli mijoz. Buyurtmangizni bergan manzilingizga yetkazib berish uchun yo'lga chiqdi.";

    String WAITING_PHONE = "⏩️ Telefonga javob berishni kuting";
}
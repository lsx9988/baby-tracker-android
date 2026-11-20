package com.babytracker.app.domain.model

/** 喂养类型（原创分类，单位由类型决定） */
enum class FeedType(val label: String, val unit: String?) {
    BOTTLE("奶瓶", "ml"),
    BREAST_LEFT("母乳·左", "分钟"),
    BREAST_RIGHT("母乳·右", "分钟"),
    SOLID("辅食", "份"),
    WATER("喝水", "ml"),
    PUMP("吸奶", "ml");

    companion object {
        fun fromName(name: String?): FeedType =
            values().firstOrNull { it.name == name } ?: BOTTLE
    }
}

/** 护理类型 */
enum class CareType(val label: String) {
    DIAPER("尿布"),
    SLEEP("睡眠"),
    BATH("洗澡"),
    SPIT_UP("吐奶");

    companion object {
        fun fromName(name: String?): CareType =
            values().firstOrNull { it.name == name } ?: DIAPER
    }
}

/** 健康指标类型 */
enum class HealthType(val label: String, val unit: String) {
    TEMP("体温", "℃"),
    WEIGHT("体重", "kg"),
    HEIGHT("身高", "cm"),
    HEAD("头围", "cm"),
    JAUNDICE("黄疸", "mg/dl");

    companion object {
        fun fromName(name: String?): HealthType =
            values().firstOrNull { it.name == name } ?: TEMP
    }
}

/** 医疗记录类型 */
enum class MedicalType(val label: String) {
    SUPPLEMENT("补剂"),
    MEDICINE("用药"),
    VACCINE("疫苗"),
    CHECKUP("随访"),
    VISIT("就医");

    companion object {
        fun fromName(name: String?): MedicalType =
            values().firstOrNull { it.name == name } ?: SUPPLEMENT
    }
}

/** 宝宝性别 */
enum class Gender(val label: String) {
    UNKNOWN("未填"),
    BOY("男"),
    GIRL("女");

    companion object {
        fun fromName(name: String?): Gender =
            values().firstOrNull { it.name == name } ?: UNKNOWN
    }
}

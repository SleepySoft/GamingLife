package glcore
import android.os.Build
import android.support.annotation.RequiresApi
import java.time.LocalDateTime


class TimeSection {
    @RequiresApi(Build.VERSION_CODES.O)
    var sectionStart = LocalDateTime.now()
    var sectionClass: String = ""
    @RequiresApi(Build.VERSION_CODES.O)
    var sectionEndRef = LocalDateTime.now()
}


class GlTimeModule {

    lateinit var mContext: GlContext
    lateinit var mCurrentSection: TimeSection

    fun init(context: GlContext) {
        mContext = context
        mCurrentSection = TimeSection().apply {
            this.sectionClass = "5084b76d-4e75-4c44-9786-bdf94075f94d"
        }
    }

}
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.jakewharton.rxrelay3.PublishRelay
import io.reactivex.rxjava3.core.Observable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class MinificationSample(
    @SerializedName("value") val value: String,
) : Parcelable {

    @IgnoredOnParcel
    private var valueChangesRelay: PublishRelay<Parameter<*>>? = null

    val valueChanges: Observable<Parameter<*>>
        get() = valueChangesRelay ?: createValueChangesRelay().also { valueChangesRelay = it }

    private fun createValueChangesRelay(): PublishRelay<Parameter<*>> {
        val valueChangesRelay: PublishRelay<Parameter<*>> = PublishRelay.create()
        // omit logic
        return valueChangesRelay
    }
}

data class Parameter<T>(val name: String)

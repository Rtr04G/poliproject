package com.example.poli_project.screens

import android.graphics.drawable.Drawable
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.poli_project.components.ScreenWithBackButton
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.image.ImagesPlugin
import org.commonmark.node.Image
import java.io.InputStream

@Composable
fun GettingStartedScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val documentationContent = """
    # Данная документация содержит базовые знания по работе с мобильным приложением "Поликлиника".
    
    После успешной авторизации вы находитесь на главном экране. В случае, если вы пациент, то он выглядит следующим образом:
    
    ### Главный экран пациента
    Здесь отображается главное меню, в данном случае состоящее из одного пункта - записаться ко врачу.
    
    Перейдя с главного экрана на экран записи, вы увидите следующее:
    
    ### Экран записи ко врачу
    Это экран поиска врача. Используя фильтры, вы можете уточнить свои требования к специалисту.
    
    Чтобы записаться ко врачу, требуется выполнить ряд простых действий:
    1. Выберите врача из списка и нажмите на кнопку "Подробнее"
    2. Вы окажетесь на экране подробной информации о враче
    ### Информация о враче
    3. Нажмите кнопку "Выбрать дату и время"
    4. Вы окажетесь на экране выбора даты и времени
    ### Экран выбора даты и времени
    
    Выбрав дату, вы сможете выбрать свободное время для записи.
    
    ### Выбор свободного времени
    В данном случае фиолетовым выделена выбранная дата, а красным - занятая. Занятую дату нельзя выбрать.
    
    Выбрав свободные дату и время, вы сможете нажать на кнопку "Записаться!", после чего заявка будет сформирована. Поздравляем, вы записались на прием!
    
    После успешной авторизации вы находитесь на главном экране. В случае, если вы врач, то он выглядит следующим образом:
    
    ### Главный экран врача
    Посмотреть ваше текущее расписание можно, перейдя по соответствующему пункту меню.
    
    Ваше расписание отображается следующим образом:
    
    ### Ваше расписание
    Заявки на прием отображены в хронологическом порядке. Нажав на конкретную заявку, вы перейдете на экран подробного описания пациента:
    
    ### Экран подробной информации о пациенте
    На данном экране видны все документы, относящиеся к данному пациенту. Вы можете добавить новый документ, закрыть заявку (после того, как она будет успешно завершена) или отменить заявку. 
    
    Поздравляем, вы успешно освоили работу с расписанием!
    """.trimIndent()


    ScreenWithBackButton("Документация", onBack) {
        DocumentationContent(documentationContent)
    }
}

@Composable
fun DocumentationContent(content: String) {
    val context = LocalContext.current

    val markwon = remember {
        Markwon.builder(context)
            .usePlugin(ImagesPlugin.create())
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                    builder.on(Image::class.java) { visitor, image ->
                        val imageUri = image.destination
                        if (imageUri != null && imageUri.startsWith("file:///android_asset/images/")) {
                            val imagePath = imageUri.removePrefix("file:///android_asset/images/")
                            val inputStream: InputStream = context.assets.open("images/$imagePath")
                            val drawable: Drawable? = Drawable.createFromStream(inputStream, null)
                            val imageSpan = drawable?.let { android.text.style.ImageSpan(it) }

                            val start = visitor.builder().length
                            visitor.builder().append(" ")
                            if (imageSpan != null) {
                                visitor.builder().setSpan(imageSpan, start, visitor.builder().length, 0)
                            }
                        }
                    }
                }
            })
            .build()
    }

    AndroidView(
        factory = { TextView(context).apply { movementMethod = android.text.method.LinkMovementMethod.getInstance() } },
        update = { textView ->
            markwon.setMarkdown(textView, content)
        }
    )
}

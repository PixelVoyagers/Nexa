package pixel.nexa.network.command

import pixel.auxframework.component.annotation.Controller

@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Controller
annotation class Command(val name: String, val needPermission: Boolean = false)

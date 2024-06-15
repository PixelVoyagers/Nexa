package pixel.nexa.plugin.adventure.addon

import pixel.auxframework.component.annotation.Autowired
import pixel.auxframework.component.annotation.Repository
import pixel.auxframework.component.factory.AfterComponentAutowired
import pixel.auxframework.component.factory.ComponentFactory
import pixel.auxframework.component.factory.getComponents
import pixel.auxframework.context.builtin.SimpleListRepository

@Repository
abstract class AdventureAddonRepository : SimpleListRepository<AdventureAddon>, AfterComponentAutowired {

    @Autowired
    internal lateinit var componentFactory: ComponentFactory

    override fun afterComponentAutowired() {
        componentFactory.getComponents<AdventureAddon>().forEach(::add)
    }

}
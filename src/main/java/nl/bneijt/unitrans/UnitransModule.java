package nl.bneijt.unitrans;

import com.google.inject.AbstractModule;
import nl.bneijt.unitrans.blockstore.HashDirectoryTree;
import nl.bneijt.unitrans.metadata.Neo4JStorage;

public class UnitransModule extends AbstractModule {
    private final CommandlineConfiguration commandlineConfiguration;

    public UnitransModule(CommandlineConfiguration commandlineConfiguration) {
        this.commandlineConfiguration = commandlineConfiguration;
    }

    @Override
    public void configure() {
        bind(HashDirectoryTree.class).toInstance(new HashDirectoryTree(commandlineConfiguration.getDataStoreLocation()));
        bind(Neo4JStorage.class).toInstance(new Neo4JStorage(commandlineConfiguration.getMetaStoreLocation()));

        bind(CommandlineConfiguration.class).toInstance(commandlineConfiguration);
    }
}

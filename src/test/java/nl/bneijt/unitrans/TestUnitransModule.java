package nl.bneijt.unitrans;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import nl.bneijt.unitrans.metadata.Neo4JStorage;

import static org.mockito.Mockito.mock;

public class TestUnitransModule extends AbstractModule {
    public static Injector createInjector() {
        return Guice.createInjector(new TestUnitransModule());
    }
    @Override
    protected void configure() {
        bind(Neo4JStorage.class).toInstance(mock(Neo4JStorage.class));
        bind(CommandlineConfiguration.class).toInstance(mock(CommandlineConfiguration.class));
    }
}

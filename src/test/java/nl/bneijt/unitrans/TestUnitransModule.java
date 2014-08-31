package nl.bneijt.unitrans;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import nl.bneijt.unitrans.accesscontrol.RootBlockPerUserDiskMap;
import nl.bneijt.unitrans.blockstore.BlockStore;
import nl.bneijt.unitrans.blockstore.StreamStore;

import static org.mockito.Mockito.mock;

public class TestUnitransModule extends AbstractModule {
    public static Injector createInjector() {
        return Guice.createInjector(new TestUnitransModule());
    }
    @Override
    protected void configure() {
        bind(BlockStore.class).to(TestBlockStore.class);
        bind(StreamStore.class).to(TestStreamStore.class);
        RootBlockPerUserDiskMap rootBlockPerUserDiskMap = mock(RootBlockPerUserDiskMap.class);
        bind(RootBlockPerUserDiskMap.class).toInstance(rootBlockPerUserDiskMap);

        CommandlineConfiguration mockCommandlineConfiguration = mock(CommandlineConfiguration.class);
        bind(CommandlineConfiguration.class).toInstance(mockCommandlineConfiguration);


    }
}

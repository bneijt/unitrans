package nl.bneijt.unitrans;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import nl.bneijt.unitrans.accesscontrol.DiskMap;
import nl.bneijt.unitrans.accesscontrol.FileDiskMap;
import nl.bneijt.unitrans.accesscontrol.RootBlockPerUserDiskMap;
import nl.bneijt.unitrans.blockstore.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class UnitransModule extends AbstractModule {
    private final CommandlineConfiguration commandlineConfiguration;

    public UnitransModule(CommandlineConfiguration commandlineConfiguration) throws FileNotFoundException {
        this.commandlineConfiguration = commandlineConfiguration;
    }

    @Override
    public void configure() {
        try {
            File blockStoreLocation = new File(commandlineConfiguration.getBlockstoreLocation());
            FileBasedBlockStoreLocation fileBasedBlockStoreLocation = new FileBasedBlockStoreLocation(blockStoreLocation);
            fileBasedBlockStoreLocation.prepare();
            bind(FileBasedBlockStoreLocation.class).toInstance(fileBasedBlockStoreLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not open blockstore", e);
        }


        try {
            RootBlockPerUserDiskMap diskMap = new RootBlockPerUserDiskMap(new File(commandlineConfiguration.getRootBlockPerUserLocation()));
            bind(RootBlockPerUserDiskMap.class).toInstance(diskMap);
        } catch (IOException e) {
            throw new RuntimeException("Could not open root block disk map", e);
        }



        bind(BlockStore.class).to(FileBasedBlockStore.class);
        bind(StreamStore.class).to(FileBasedStreamStore.class);
        bind(CommandlineConfiguration.class).toInstance(commandlineConfiguration);
    }
}

package de.m3y.hadoop.hdfs.hfsa.core;


import org.apache.hadoop.fs.permission.PermissionStatus;
import org.apache.hadoop.hdfs.server.namenode.FsImageProto;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class FSImageLoaderTest {
    private static final Logger log = LoggerFactory.getLogger(FSImageLoaderTest.class);

    Set<String> groupNames = new HashSet<>();
    Set<String> userNames = new HashSet<>();
    int sumFiles;
    int sumDirs;
    int sumSymLinks;
    long sumSize;

    @Test
    public void testLoadAndVisit() throws IOException {
        RandomAccessFile file = new RandomAccessFile("src/test/resources/fsi_small.img", "r");
        final FSImageLoader loader = FSImageLoader.load(file);


        loader.visit(new FsVisitor() {
            @Override
            public void onFile(FsImageProto.INodeSection.INode inode) {
                FsImageProto.INodeSection.INodeFile f = inode.getFile();

                PermissionStatus p = loader.getPermissionStatus(f.getPermission());
                groupNames.add(p.getGroupName());
                userNames.add(p.getUserName());
                sumFiles++;
                sumSize += FSImageLoader.getFileSize(f);
            }

            @Override
            public void onDirectory(FsImageProto.INodeSection.INode inode) {
//                System.out.println("D: " + inode.getName().toStringUtf8());
                FsImageProto.INodeSection.INodeDirectory d = inode.getDirectory();
                PermissionStatus p = loader.getPermissionStatus(d.getPermission());
                groupNames.add(p.getGroupName());
                userNames.add(p.getUserName());
                sumDirs++;
            }

            @Override
            public void onSymLink(FsImageProto.INodeSection.INode inode) {
                sumSymLinks++;
            }
        });

        assertEquals(3, userNames.size());
        assertEquals(3, groupNames.size());
        assertEquals(8, sumDirs);
        assertEquals(10, sumFiles);
        assertEquals(0, sumSymLinks);
        assertEquals(348017664L, sumSize);
    }

}
#!/usr/bin/python
#
# Script:
# Purpose:
# Author: Jochen Wuttke, wuttkej@gmail.com
# Date:

import unittest
import pre_process

class PreProcessTest(unittest.TestCase):

    def test_read_data(self):    
        infile = open("rev-sample.txt", "r")
        data = pre_process.read_data(infile)
        self.assertEqual(3, len(data))

    def test_build_rev_pair(self):
        inputs = [ "16;8378cec;d3867bf;~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/fetcher/HdfsFetcher.java;1;0;voldemort.store.slop.strategy.HandoffToAnyStrategyTest;1;1;1", "17;8378cec;d3867bf;~contrib/hadoop-store-builder/test/voldemort/store/readonly/checksum/CheckSumTests.java;1;0;voldemort.store.invalidmetadata.InvalidMetadataCheckingStoreTest;1;1;1" ]
        data = pre_process.build_rev_pair( "d3867bf","8378cec", inputs)
        self.assertEqual( 2, len(data.mixedRevisions) )
        inputs = ["21;b17f572;5400077;~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/MD5CheckSum.java;1;0;voldemort.store.slop.strategy.HandoffToAnyStrategyTest;1;1;1", "22;b17f572;5400077;~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/CheckSum.java;1;0;voldemort.store.invalidmetadata.InvalidMetadataCheckingStoreTest;1;1;1", "23;b17f572;5400077;~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/Adler32CheckSum.java;1;0;voldemort.store.invalidmetadata.InvalidMetadataCheckingStoreTest;1;1;1" ]
        data = pre_process.build_rev_pair("5400077", "b17f572",inputs)
        self.assertEqual( 3, len(data.mixedRevisions) )

    def test_build_mix(self):
        input = [ "30;b17f572;5400077;~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/CheckSum.java,+contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/MD5CheckSum.java,~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/CRC32CheckSum.java;1;0;voldemort.store.routed.NodeValueTest;1;0;1", "30;b17f572;5400077;~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/CheckSum.java,~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/MD5CheckSum.java,~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/CRC32CheckSum.java;1;0;voldemort.store.http.HttpStoreTest;1;1;1"]
        data = pre_process.build_mix(30, input)
        self.assertEqual(3, len(data.changedFiles))
        self.assertEqual("contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/CheckSum.java", data.changedFiles[0].fileName)
        self.assertEqual("~", data.changedFiles[0].changeType)
        self.assertEqual("contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/MD5CheckSum.java", data.changedFiles[1].fileName)
        self.assertEqual("+", data.changedFiles[1].changeType)
        self.assertEqual( 2, len(data.tests) )
        self.assertEqual( "voldemort.store.routed.NodeValueTest", data.tests[0].testName)
        self.assertEquals(0, data.tests[0].childResult)
        self.assertEquals(1, data.tests[1].childResult)
        

if __name__ == "__main__":
	unittest.main()

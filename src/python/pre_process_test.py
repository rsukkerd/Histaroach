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

    def test_rev_pair_non_compileable(self):
        input = [ "19;1ee8246;23c9b28;~test/unit/voldemort/store/readonly/ReadOnlyStorageEngineTestInstance.java;0;0;n;n;n;n" ]
        data = pre_process.build_rev_pair("23c9b28", "1ee8246", input)
        self.assertEqual( 1, len(data.mixedRevisions))

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

    def test_mixed_rev_is_repaired(self):
        input = [ "16;8378cec;d3867bf;~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/fetcher/HdfsFetcher.java;1;0;voldemort.store.routed.HintedHandoffTest;1;0;1", "16;8378cec;d3867bf;~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/fetcher/HdfsFetcher.java;1;0;voldemort.protocol.pb.ProtocolBuffersRequestFormatTest;1;1;1"] 
        mix = pre_process.build_mix(16, input)
        self.assertTrue(mix.is_repaired())
        input = [ "17;8378cec;d3867bf;~contrib/hadoop-store-builder/test/voldemort/store/readonly/checksum/CheckSumTests.java;1;0;voldemort.store.routed.HintedHandoffTest;1;0;1", "17;8378cec;d3867bf;~contrib/hadoop-store-builder/test/voldemort/store/readonly/checksum/CheckSumTests.java;1;0;voldemort.protocol.pb.ProtocolBuffersRequestFormatTest;0;1;1"] 
        mix = pre_process.build_mix(17, input)
        self.assertFalse(mix.is_repaired())
        
        
    def test_rev_pair_is_repaired(self):
        #mix 16 repairs flip in HintedHandoffTest
        infile = open("rev-sample.txt", "r")
        data = pre_process.read_data(infile)
        self.assertFalse(data[0].is_repaired())
        self.assertTrue(data[2].is_repaired())

    def test_get_all_files(self):
        input = [ "18;1ee8246;23c9b28;~src/java/voldemort/store/readonly/ReadOnlyStorageEngine.java;1;0;voldemort.store.readonly.ReadOnlyStorageEngineTest;0;0;1", "18;1ee8246;23c9b28;~src/java/voldemort/store/readonly/ReadOnlyStorageEngine.java;1;0;voldemort.client.rebalance.RebalanceTest;1;0;1", "18;1ee8246;23c9b28;~src/java/voldemort/store/readonly/ReadOnlyStorageEngine.java;1;0;voldemort.store.compress.CompressingStoreTest;0;0;0", "19;1ee8246;23c9b28;~test/unit/voldemort/store/readonly/ReadOnlyStorageEngineTestInstance.java;0;0;n;n;n;n" ]
        data = pre_process.build_rev_pair( "23c9b28", "1ee8246", input)
        self.assertEqual(2, len(data.mixedRevisions))
        self.assertEqual(2, len(data.get_all_files()) )

    def test_rev_pair_get_delta_p_bar(self):
        input = [ "18;1ee8246;23c9b28;~src/java/voldemort/store/readonly/ReadOnlyStorageEngine.java;1;0;voldemort.store.readonly.ReadOnlyStorageEngineTest;0;0;1", "18;1ee8246;23c9b28;~src/java/voldemort/store/readonly/ReadOnlyStorageEngine.java;1;0;voldemort.client.rebalance.RebalanceTest;1;0;1", "18;1ee8246;23c9b28;~src/java/voldemort/store/readonly/ReadOnlyStorageEngine.java;1;0;voldemort.store.compress.CompressingStoreTest;0;0;0", "19;1ee8246;23c9b28;~test/unit/voldemort/store/readonly/ReadOnlyStorageEngineTestInstance.java;0;0;n;n;n;n" ]
        data = pre_process.build_rev_pair( "23c9b28", "1ee8246", input)
        self.assertTrue( data.is_repaired() )
        self.assertEqual( 1, len(data.get_delta_p_bar()) )

if __name__ == "__main__":
	unittest.main()

#!/usr/bin/python
#
# Script:
# Purpose:
# Author: Jochen Wuttke, wuttkej@gmail.com
# Date:

import unittest
import pre_process

class PreProcessTest(unittest.TestCase):
    
    def make_lines(self, inputs):
        lines = []
        for line in inputs:
            lines.append(pre_process.InputFileLine(line.split(";")))
        return lines

    def test_delta_class(self):
        delta = pre_process.Delta("012ba09;b6266cd;Msrc/java/voldemort/store/bdb/BdbStorageConfiguration.java,Msrc/java/voldemort/server/VoldemortConfig.java")
        self.assertEqual("012ba09", delta.parentID)
        self.assertEqual("b6266cd", delta.childID)
        self.assertEqual(2, len(delta.totalDelta))
        self.assertEqual("src/java/voldemort/server/VoldemortConfig.java", delta.totalDelta[1].fileName)

    def test_read_deltas(self):
        deltas = pre_process.read_deltas("delta_test.input")
        self.assertEqual( 3, len(deltas))
        self.assertEqual( "a37abfb", deltas[1].parentID )
        self.assertEqual( "67443e6", deltas[2].childID )

    def test_get_delta(self):
        deltas = pre_process.read_deltas("delta_test.input")
        delta = pre_process.get_delta(deltas, "a37abfb", "d3867bf")
        self.assertEqual( "src/java/voldemort/store/readonly/JsonStoreBuilder.java", delta.totalDelta[0].fileName)
        delta = pre_process.get_delta( deltas, "516bc3a", "xxx")
        self.assertIsNone( delta )
    
    def test_data_file_format(self):
        infile = open("rev-sample.txt", "r")
        line = infile.readlines()[2]
        infile.close()
        data = pre_process.InputFileLine(line.split(";"))
        self.assertEqual( 1, data.mixID)
        self.assertEqual( "012ba09", data.parentID)
        self.assertEqual( "b6266cd", data.childID)
        self.assertEqual( "Msrc/java/voldemort/server/VoldemortConfig.java", data.changedFiles)
        self.assertTrue(data.compilable)
        self.assertFalse( data.aborted)
        self.assertEqual( "voldemort.store.invalidmetadata.InvalidMetadataCheckingStoreTest", data.testName)
        self.assertEqual( "1", data.parentResult)
        self.assertEqual( "1", data.childResult)
        self.assertEqual( "1", data.mixResult)

    def test_read_data(self):    
        infile = open("rev-sample.txt", "r")
        data = pre_process.read_data(infile)
        self.assertEqual(17, len(data))

    def test_read_data_long(self):
        data = pre_process.read_data( open("read_data.txt","r"))
        pre_process.check_all_revs(pre_process.get_all_mixes(data))
        #self.assertEqual( 258, pre_process.get_num_mixes(data))

    def test_build_rev_pair(self):
        inputs = [ "16;8378cec;d3867bf;~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/fetcher/HdfsFetcher.java;1;0;voldemort.store.slop.strategy.HandoffToAnyStrategyTest;1;1;1", "17;8378cec;d3867bf;~contrib/hadoop-store-builder/test/voldemort/store/readonly/checksum/CheckSumTests.java;1;0;voldemort.store.invalidmetadata.InvalidMetadataCheckingStoreTest;1;1;1" ]
        in_lines = self.make_lines(inputs)
        data = pre_process.build_rev_pair( "d3867bf","8378cec", in_lines)
        self.assertEqual( 2, len(data.mixedRevisions) )
        inputs = ["21;b17f572;5400077;~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/MD5CheckSum.java;1;0;voldemort.store.slop.strategy.HandoffToAnyStrategyTest;1;1;1", "22;b17f572;5400077;~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/CheckSum.java;1;0;voldemort.store.invalidmetadata.InvalidMetadataCheckingStoreTest;1;1;1", "23;b17f572;5400077;~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/Adler32CheckSum.java;1;0;voldemort.store.invalidmetadata.InvalidMetadataCheckingStoreTest;1;1;1" ]
        in_lines = self.make_lines(inputs)
        data = pre_process.build_rev_pair("5400077", "b17f572",in_lines)
        self.assertEqual( 3, len(data.mixedRevisions) )

    def test_rev_pair_non_compileable(self):
        inputs = [ "19;1ee8246;23c9b28;~test/unit/voldemort/store/readonly/ReadOnlyStorageEngineTestInstance.java;0;0;n;n;n;n" ]
        in_lines = self.make_lines(inputs)
        data = pre_process.build_rev_pair("23c9b28", "1ee8246", in_lines)
        self.assertEqual( 1, len(data.mixedRevisions))

    def test_build_mix(self):
        inputs = [ "30;b17f572;5400077;~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/CheckSum.java,+contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/MD5CheckSum.java,~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/CRC32CheckSum.java;1;0;voldemort.store.routed.NodeValueTest;1;0;1", "30;b17f572;5400077;~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/CheckSum.java,~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/MD5CheckSum.java,~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/CRC32CheckSum.java;1;0;voldemort.store.http.HttpStoreTest;1;1;1"]
        in_lines = self.make_lines(inputs)
        data = pre_process.build_mix(30, in_lines)
        self.assertEqual(3, len(data.revertedFiles))
        self.assertEqual("contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/CheckSum.java", data.revertedFiles[0].fileName)
        self.assertEqual("~", data.revertedFiles[0].changeType)
        self.assertEqual("contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/MD5CheckSum.java", data.revertedFiles[1].fileName)
        self.assertEqual("+", data.revertedFiles[1].changeType)
        self.assertEqual( 2, len(data.tests) )
        self.assertEqual( "voldemort.store.routed.NodeValueTest", data.tests[0].testName)
        self.assertEquals(0, data.tests[0].parentResult)
        self.assertEquals(1, data.tests[1].childResult)

    def test_mixed_rev_is_repaired(self):
        inputs = [ "16;8378cec;d3867bf;~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/fetcher/HdfsFetcher.java;1;0;voldemort.store.routed.HintedHandoffTest;1;1;0", "16;8378cec;d3867bf;~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/fetcher/HdfsFetcher.java;1;0;voldemort.protocol.pb.ProtocolBuffersRequestFormatTest;1;1;1"] 
        in_lines = self.make_lines(inputs)
        mix = pre_process.build_mix(16, in_lines)
        self.assertTrue(mix.is_repaired())
        inputs = [ "17;8378cec;d3867bf;~contrib/hadoop-store-builder/test/voldemort/store/readonly/checksum/CheckSumTests.java;1;0;voldemort.store.routed.HintedHandoffTest;1;0;1", "17;8378cec;d3867bf;~contrib/hadoop-store-builder/test/voldemort/store/readonly/checksum/CheckSumTests.java;1;0;voldemort.protocol.pb.ProtocolBuffersRequestFormatTest;0;1;1"] 
        in_lines = self.make_lines(inputs)
        mix = pre_process.build_mix(17, in_lines)
        self.assertFalse(mix.is_repaired())
        
        
    def test_rev_pair_is_repaired(self):
        #mix 16 repairs flip in HintedHandoffTest
        inputs = [ "16;8378cec;d3867bf;~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/fetcher/HdfsFetcher.java;1;0;voldemort.store.routed.HintedHandoffTest;1;1;0", "16;8378cec;d3867bf;~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/fetcher/HdfsFetcher.java;1;0;voldemort.protocol.pb.ProtocolBuffersRequestFormatTest;1;1;1"] 
        in_lines = self.make_lines(inputs)
        data = pre_process.build_rev_pair( "8378ce", "d3867bf", in_lines)
        self.assertTrue(data.is_repaired())

    def test_rev_pair_get_delta_p_bar(self):
        inputs = [ "18;1ee8246;23c9b28;~src/java/voldemort/store/readonly/ReadOnlyStorageEngine.java;1;0;voldemort.store.readonly.ReadOnlyStorageEngineTest;0;1;0", "18;1ee8246;23c9b28;~src/java/voldemort/store/readonly/ReadOnlyStorageEngine.java;1;0;voldemort.client.rebalance.RebalanceTest;1;1;0", "18;1ee8246;23c9b28;~src/java/voldemort/store/readonly/ReadOnlyStorageEngine.java;1;0;voldemort.store.compress.CompressingStoreTest;0;0;0", "19;1ee8246;23c9b28;~test/unit/voldemort/store/readonly/ReadOnlyStorageEngineTestInstance.java;0;0;n;n;n;n" ]
        in_lines = self.make_lines(inputs)
        data = pre_process.build_rev_pair("1ee8246", "23c9b28", in_lines)
        self.assertTrue( data.is_repaired() )
        deltas = [pre_process.Delta("1ee8246;23c9b28;Msrc/java/voldemort/store/readonly/ReadOnlyStorageEngine.java,Mtest/unit/voldemort/store/readonly/ReadOnlyStorageEngineTestInstance.java")]
        self.assertEqual( 1, len(data.get_delta_p_bar(deltas[0])) )

    def test_rev_pair_get_delta_p(self):
        inputs = [ "21;b17f572;5400077;~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/MD5CheckSum.java;1;0;voldemort.client.CachingStoreClientFactoryTest;1;1;1", "21;b17f572;5400077;~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/MD5CheckSum.java;1;0;voldemort.server.EndToEndTest;1;1;0", "32;b17f572;5400077;~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/CheckSum.java,~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/Adler32CheckSum.java,~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/CRC32CheckSum.java;1;0;voldemort.client.CachingStoreClientFactoryTest;1;1;1", "32;b17f572;5400077;~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/CheckSum.java,~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/Adler32CheckSum.java,~contrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/CRC32CheckSum.java;1;0;voldemort.server.EndToEndTest;1;1;0" ]
        in_lines = self.make_lines(inputs)
        data = pre_process.build_rev_pair( "5400077", "b17f572", in_lines )
        deltas = [pre_process.Delta("5400077;b17f572;Mcontrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/MD5CheckSum.java,Mcontrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/CheckSum.java,Mcontrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/Adler32CheckSum.java,Mcontrib/hadoop-store-builder/src/java/voldemort/store/readonly/checksum/CRC32CheckSum.java" )]
        self.assertEqual( 4, len(pre_process.get_delta( deltas, "5400077", "b17f572").totalDelta ) )
        self.assertEqual( 1, len(data.get_delta_p()) )
        self.assertEqual( 32, data.get_delta_p()[0].mixID )

    def test_delta_p_bar_details(self):
        inputs = [ "8;ca9f374;7857afa;Msrc/java/voldemort/client/protocol/admin/AdminClient.java;1;0;voldemort.store.metadata.MetadataStoreTest;1;1;1", "8;ca9f374;7857afa;Msrc/java/voldemort/client/protocol/admin/AdminClient.java;1;0;voldemort.store.routed.HintedHandoffTest;1;1;0", "8;ca9f374;7857afa;Msrc/java/voldemort/client/protocol/admin/AdminClient.java;1;0;voldemort.protocol.pb.ProtocolBuffersRequestFormatTest;1;1;1" ]
        in_lines = self.make_lines(inputs)
        data = pre_process.build_rev_pair( "ca9f374", "7857afa", in_lines )
        deltas = [pre_process.Delta( "ca9f374;7857afa;Msrc/java/voldemort/client/protocol/admin/AdminClient.java,Msrc/java/voldemort/utils/RebalanceUtils.java" ) ]
        d_bar = data.get_delta_p_bar(deltas[0])
        self.assertEqual(1, len(d_bar[0].revertedFiles) )
    
    def test_changed_file_equality(self):
        ch1 = pre_process.ChangedFile( "someName/wiht/slashes", "M" )
        ch2 = pre_process.ChangedFile( "someName/wiht/slashes", "M" )
        self.assertEqual(ch1, ch2)
        ch3 = pre_process.ChangedFile( "someName/with/slashes", "M" )
        self.assertNotEqual(ch1, ch3)

if __name__ == "__main__":
	unittest.main()

package tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import common.RepositoryOld;
import common.TestResult;

public class ProcessResultTest {
    private static final String[] ALL_JUNIT_TESTS = {
            "voldemort.client.AdminServiceBasicTest",
            "voldemort.client.AdminServiceFailureTest",
            "voldemort.client.AdminServiceFilterTest",
            "voldemort.client.AdminServiceMultiJVMTest",
            "voldemort.client.CachingStoreClientFactoryTest",
            "voldemort.client.DefaultStoreClientTest",
            "voldemort.client.HttpStoreClientFactoryTest",
            "voldemort.client.LazyStoreClientTest",
            "voldemort.client.SocketStoreClientFactoryTest",
            "voldemort.client.rebalance.AdminRebalanceTest",
            "voldemort.client.rebalance.RebalanceClusterPlanTest",
            "voldemort.client.rebalance.RebalancePartitionsInfoTest",
            "voldemort.client.rebalance.RebalanceTest",
            "voldemort.cluster.failuredetector.BannagePeriodFailureDetectorTest",
            "voldemort.cluster.failuredetector.ServerStoreVerifierTest",
            "voldemort.cluster.failuredetector.ThresholdFailureDetectorTest",
            "voldemort.protocol.pb.ProtocolBuffersRequestFormatTest",
            "voldemort.protocol.vold.VoldemortNativeRequestFormatTest",
            "voldemort.routing.ConsistentRoutingStrategyTest",
            "voldemort.routing.ZoneRoutingStrategyTest",
            "voldemort.scheduled.BlockingSlopPusherTest",
            "voldemort.scheduled.DataCleanupJobTest",
            "voldemort.scheduled.StreamingSlopPusherTest",
            "voldemort.serialization.SlopSerializerTest",
            "voldemort.serialization.VersionedSerializerTest",
            "voldemort.serialization.avro.AvroGenericSerializerTest",
            "voldemort.serialization.avro.AvroReflectiveSerializerTest",
            "voldemort.serialization.avro.AvroSpecificSerializerTest",
            "voldemort.serialization.json.JsonBackwardsCompatibilityTest",
            "voldemort.serialization.json.JsonReaderTest",
            "voldemort.serialization.json.JsonTypeSerializerTest",
            "voldemort.serialization.protobuf.ProtoBufSerializerTest",
            "voldemort.serialization.thrift.ThriftSerializerTest",
            "voldemort.server.EndToEndTest", "voldemort.server.ServiceTest",
            "voldemort.server.gossip.GossiperTest",
            "voldemort.server.protocol.admin.AsyncOperationTest",
            "voldemort.server.rebalance.RebalancerStateTest",
            "voldemort.server.socket.ClientRequestExecutorPoolTest",
            "voldemort.server.socket.SocketPoolTest",
            "voldemort.server.socket.SocketServerTest",
            "voldemort.server.storage.StorageServiceTest",
            "voldemort.store.bdb.BdbSplitStorageEngineTest",
            "voldemort.store.bdb.BdbStorageEngineTest",
            "voldemort.store.compress.CompressingStoreTest",
            "voldemort.store.configuration.ConfigurationStorageEngineTest",
            "voldemort.store.http.HttpStoreTest",
            "voldemort.store.invalidmetadata.InvalidMetadataCheckingStoreTest",
            "voldemort.store.invalidmetadata.ServerSideRoutingTest",
            "voldemort.store.logging.LoggingStoreTest",
            "voldemort.store.memory.CacheStorageEngineTest",
            "voldemort.store.memory.InMemoryStorageEngineTest",
            "voldemort.store.metadata.MetadataStoreTest",
            "voldemort.store.mysql.MysqlStorageEngineTest",
            "voldemort.store.readonly.ExternalSorterTest",
            "voldemort.store.readonly.ReadOnlyStorageEngineTest",
            "voldemort.store.readonly.ReadOnlyStorageMetadataTest",
            "voldemort.store.readonly.ReadOnlyUtilsTest",
            "voldemort.store.readonly.SearchStrategyTest",
            "voldemort.store.readonly.chunk.DataFileChunkSetIteratorTest",
            "voldemort.store.readonly.swapper.StoreSwapperTest",
            "voldemort.store.rebalancing.RebootstrappingStoreTest",
            "voldemort.store.rebalancing.RedirectingStoreTest",
            "voldemort.store.routed.HintedHandoffTest",
            "voldemort.store.routed.NodeValueTest",
            "voldemort.store.routed.ReadRepairerTest",
            "voldemort.store.routed.RoutedStoreTest",
            "voldemort.store.routed.action.ConfigureNodesTest",
            "voldemort.store.routed.action.GetAllConfigureNodesTest",
            "voldemort.store.serialized.SerializingStoreTest",
            "voldemort.store.slop.SlopTest",
            "voldemort.store.slop.strategy.ConsistentHandoffStrategyTest",
            "voldemort.store.slop.strategy.HandoffToAnyStrategyTest",
            "voldemort.store.slop.strategy.ProximityHandoffStrategyTest",
            "voldemort.store.socket.ProtocolBuffersSocketStoreTest",
            "voldemort.store.socket.VoldemortNativeSocketStoreTest",
            "voldemort.store.stats.StatsTest",
            "voldemort.store.stats.StoreStatsJmxTest",
            "voldemort.store.views.ViewStorageEngineTest",
            "voldemort.store.views.ViewTransformsTest",
            "voldemort.utils.ByteUtilsTest",
            "voldemort.utils.CachedCallableTest",
            "voldemort.utils.IoThrottlerTest",
            "voldemort.utils.NetworkClassLoaderTest",
            "voldemort.utils.RebalanceUtilsTest",
            "voldemort.utils.ReflectUtilsTest", "voldemort.utils.UtilsTest",
            "voldemort.utils.pool.KeyedResourcePoolTest",
            "voldemort.versioning.ClockEntryTest",
            "voldemort.versioning.InconsistentDataExceptionTest",
            "voldemort.versioning.VectorClockInconsistencyResolverTest",
            "voldemort.versioning.VectorClockTest",
            "voldemort.versioning.VersionedTest",
            "voldemort.xml.ClusterMapperTest",
            "voldemort.xml.StoreDefinitionMapperTest", };

    private static final String[] JUNIT_FAILURES = {
            "voldemort.client.AdminServiceBasicTest",
            "voldemort.client.AdminServiceFailureTest",
            "voldemort.client.AdminServiceFilterTest",
            "voldemort.client.AdminServiceMultiJVMTest",
            "voldemort.client.SocketStoreClientFactoryTest",
            "voldemort.client.rebalance.AdminRebalanceTest",
            "voldemort.client.rebalance.RebalanceTest",
            "voldemort.cluster.failuredetector.ThresholdFailureDetectorTest",
            "voldemort.server.gossip.GossiperTest",
            "voldemort.server.socket.ClientRequestExecutorPoolTest",
            "voldemort.server.socket.SocketPoolTest",
            "voldemort.store.compress.CompressingStoreTest",
            "voldemort.store.invalidmetadata.ServerSideRoutingTest",
            "voldemort.store.mysql.MysqlStorageEngineTest",
            "voldemort.store.rebalancing.RedirectingStoreTest",
            "voldemort.store.routed.RoutedStoreTest",
            "voldemort.store.socket.ProtocolBuffersSocketStoreTest",
            "Test voldemort.store.socket.VoldemortNativeSocketStoreTest", };

    @Test
    public void testGetTestResult() {
        String arg = System.getProperty("directory");
        File directory = new File(arg);

        Set<String> allTests = new HashSet<String>();
        for (String test : ALL_JUNIT_TESTS) {
            allTests.add(test);
        }

        Set<String> failures = new HashSet<String>();
        for (String test : JUNIT_FAILURES) {
            failures.add(test);
        }

        TestResult expectedTestResult = new TestResult("master", allTests,
                failures);
        RepositoryOld repo = new RepositoryOld(directory);

        TestResult actualTestResult = repo.getTestResult("master");

        assertEquals(expectedTestResult, actualTestResult);
    }
}

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.errors.InvalidReplicationFactorException;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class MyKafkaAdmin {
    private Properties props;
    private Admin admin;

    public MyKafkaAdmin(Properties props){
        this.props = props;
        admin = Admin.create(props);
    }

    public CreateTopicsResult createTopic(String name, int partitions, short replicationFactor) throws InvalidReplicationFactorException{
        CreateTopicsResult result = admin.createTopics(Collections.singleton(
                new NewTopic(name, partitions, replicationFactor)
                        .configs(Collections.singletonMap(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT))));
        return result;
    }

    public boolean topicStatus(String topicName){
        DescribeTopicsResult res = admin.describeTopics(Collections.singleton(topicName));
        try{
            TopicDescription description = res.topicNameValues().get(topicName).get();
            System.out.println(description.partitions().stream().count());
            return true;
        } catch (ExecutionException | InterruptedException e) {
            return false;
        }
    }

    public long offsetStatus(String topicName, int partition) throws ExecutionException, InterruptedException {
        TopicPartition topicPartition = new TopicPartition(topicName, partition);
        Map<TopicPartition,OffsetSpec> topic = new HashMap<>();
        topic.put(topicPartition, OffsetSpec.latest());
        Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> latestOffsets =
                admin.listOffsets(topic).all().get();
        return  latestOffsets.get(topicPartition).offset();
    }

    public DeleteTopicsResult deleteTopic(String name){
        DeleteTopicsResult result = admin.deleteTopics(Collections.singleton(name));
        return result;
    }

    public boolean increasePartiotions(String topicName, int numPart) throws ExecutionException, InterruptedException {
        Map<String, NewPartitions> partModify = new HashMap<>();
        partModify.put(topicName, NewPartitions.increaseTo(numPart));
        CreatePartitionsResult res = admin.createPartitions(partModify);
        DescribeTopicsResult res2 = admin.describeTopics(Collections.singleton(topicName));
        TopicDescription description = res2.topicNameValues().get(topicName).get();
        return description.partitions().stream().count()==numPart;
    }
}

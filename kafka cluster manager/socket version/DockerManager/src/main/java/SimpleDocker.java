public class SimpleDocker {
    static DockerAdmin docker = null;
    public static void main(String args[]){
        try {
            docker = new DockerAdmin("administrator-administrator-1", "administrator_spark_kafka");
            docker.createWorker();
        } catch (Exception e) {
            exit(e.getMessage());
        }
        docker.close();
    }
    public static void exit(String e){
        if(docker!=null) {
            docker.close();
        }
        System.err.println(e);
        System.exit(1);
    }
}

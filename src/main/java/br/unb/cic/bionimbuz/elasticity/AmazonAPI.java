package br.unb.cic.bionimbuz.elasticity;

import br.unb.cic.bionimbuz.elasticity.legacy.Ec2Commands;
import static br.unb.cic.bionimbuz.elasticity.legacy.Ec2Commands.EC2;
import static br.unb.cic.bionimbuz.elasticity.legacy.Ec2Commands.instanceid;
import static br.unb.cic.bionimbuz.elasticity.legacy.Ec2Commands.user_input;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.model.CreateImageRequest;
import com.amazonaws.services.ec2.model.CreateImageResult;
import com.amazonaws.services.ec2.model.CreateSnapshotRequest;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

import java.io.*;
import java.util.*;

public class AmazonAPI  {

    public static AmazonEC2 EC2;
    public static Scanner user_input = new Scanner(System.in);
    public static String instanceid;
    public static KeyPair keyPair;
    public static int count = 1;
    private String ipInstance;

    public void setup() {
        try {
            String credentialsFile = System.getProperty("user.home") + "/BionimbuzClient/target/BionimbuzClient-0.0.1-SNAPSHOT/resources/apiCredentials/AwsCredentials.properties";
            InputStream is = null;
            is = new FileInputStream(credentialsFile);
            PropertiesCredentials credentials = new PropertiesCredentials(is);

            EC2 = new AmazonEC2Client(credentials);
            EC2.setEndpoint("ec2.sa-east-1.amazonaws.com");

        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }//, IllegalArgumentException
    }

    public String createinstance(String type, String imageID) throws IOException {
        // Acho que tem que mudar isso aqui em    
        //imageID = "ami-912db2fd";
        this.setup();
        String IP = null;
        try {

            // CREATE EC2 INSTANCES
            RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
                    .withInstanceType(type)
                    .withImageId(imageID)
                    .withMinCount(1)
                    .withMaxCount(1)
                    .withSecurityGroupIds("default");
            
            runInstancesRequest.setMonitoring(Boolean.TRUE);
            
            //.withKeyName("xebia-france")
            //.withUserData(Base64.encodeBase64String(myUserData.getBytes()))

//            System.out.println("Criando nova maquina BioninbuZ");
//            //String imageId = "ami-6e3bb102";
//            String imageId = "ami-912db2fd";
//
//            int minInstanceCount = 1; // 
//            int maxInstanceCount = 1;
//            RunInstancesRequest rir = new RunInstancesRequest(imageId, minInstanceCount, maxInstanceCount);
//            rir.setInstanceType(type);
//            rir.withSecurityGroups("default");
//            //rir.setMonitoring(Boolean.TRUE);
            //RunInstancesResult result = AmazonAPI.EC2.runInstances(rir);
            RunInstancesResult result = AmazonAPI.EC2.runInstances(runInstancesRequest);

            System.out.println("waiting");
                        
            System.out.println("OK");

            List<com.amazonaws.services.ec2.model.Instance> resultInstance = result.getReservation().getInstances();

            String createdInstanceId = null;
            int idx = 1;
            
            for (com.amazonaws.services.ec2.model.Instance ins : resultInstance) {

                createdInstanceId = ins.getInstanceId();

                System.out.println("New instance has been created: " + createdInstanceId);//print the instance ID
                
                CreateTagsRequest createTagsRequest = new CreateTagsRequest();
                createTagsRequest.withResources(createdInstanceId) //
                .withTags(new Tag("Name", "BioNimbuZ-" + idx));
                EC2.createTags(createTagsRequest);

                idx++;

                DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(ins.getInstanceId());
                DescribeInstancesResult result2 = EC2.describeInstances(request);
                List<Reservation> list = result2.getReservations();
            
                long startTime = System.currentTimeMillis();    
            while (IP == null){
                for (Reservation res : list) {
                    List<com.amazonaws.services.ec2.model.Instance> instanceList = res.getInstances();

                    for (com.amazonaws.services.ec2.model.Instance instance : instanceList) {
                        
                        IP = instance.getPublicIpAddress();
                        
                        
                        setIpInstance(instance.getPublicIpAddress());
                    }
                }
            }
            long estimatedTime = System.currentTimeMillis() - startTime;
            
            System.out.println("Instance Public IP :" + IP + " com tempo:" + estimatedTime);
            
            
            
            }
        } catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage());
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
            System.out.println("Give a valid input");
            System.out.println("");
//		AmazonAPI.enteroption();
        }
        return IP;
    }

    public static List<com.amazonaws.services.ec2.model.Instance> listinstances() {
        AmazonAPI api = new AmazonAPI();
        api.setup();

        DescribeInstancesResult describeInstancesRequest = EC2.describeInstances();
        List<Reservation> reservations = describeInstancesRequest.getReservations();
        List<com.amazonaws.services.ec2.model.Instance> instances = new ArrayList<>();

        for (Reservation reservation : reservations) {
            instances.addAll(reservation.getInstances());
        }

        return instances;

    }

    public String getIpInstance() {
        return ipInstance;
    }

    public void setIpInstance(String ipInstance) {
        this.ipInstance = ipInstance;
    }
    
    public void terminate(String instanceid)  {
        this.setup();
        TerminateInstancesRequest tir = new TerminateInstancesRequest()
                .withInstanceIds(instanceid);
        EC2.terminateInstances(tir);
        System.out.println("Terminating the instance : " + instanceid);
    }

    
    public String createami(String instanceid) throws IOException {
        this.setup();

            //CreateSnapshotRequest ssr = new CreateSnapshotRequest(instanceid, instanceid);
            CreateImageRequest cir = new CreateImageRequest(instanceid, instanceid);
            CreateImageResult cires = EC2.createImage(cir);
            String imageid;
            imageid = cires.getImageId();
            String imageID = cires.getImageId();
            System.out.println("The imageid of the newly created AMI is " + imageid);

            try {
                Thread.sleep(120000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        return imageID;
    }
            
    public void executeElasticity(String id) throws IOException        {
        this.setup();
        //this.createami(id);
        this.createinstance("t2.micro", this.createami(id));
        this.terminate(id);
        
    
    }


    
    

} //main end
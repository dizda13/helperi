import org.apache.sqoop.client.SqoopClient;
import org.apache.sqoop.client.SubmissionCallback;
import org.apache.sqoop.model.*;
import org.apache.sqoop.submission.counter.Counter;
import org.apache.sqoop.submission.counter.CounterGroup;
import org.apache.sqoop.submission.counter.Counters;
import org.apache.sqoop.validation.Status;

import java.util.List;

/**
 * Created by hadoop on 11/16/16.
 */
public class main {

    static void main(String[] args){
        String tableName_import="neka_stara_tabela";
        String tableName_export="neka_nova_tabela";
        String shema_import="ime_za_import";
        String shema_export="ime_za_export";
        String input_outputDir="kratica/do/direktorija";
        String dateTypeInQuery="neki_tip";
        sqoopImport(tableName_import,dateTypeInQuery,shema_import,input_outputDir);
        sqoopExport(tableName_export,shema_export,input_outputDir);
    }

    private static void sqoopImport(String tableName, String typeOfDate, String shema, String outputDir){
        String url = "http://localhost:12000/sqoop/";
        SqoopClient client = new SqoopClient(url);

        MConnection newCon = client.newConnection(1);
        System.out.println(tableName);
        //Get connection and framework forms. Set name for connection
        MConnectionForms conForms = newCon.getConnectorPart();
        MConnectionForms frameworkForms = newCon.getFrameworkPart();
        newCon.setName("MyImportConnection");

        //Set connection forms values
        conForms.getStringInput("connection.connectionString").setValue("jdbc:mysql://localhost:3306/"+shema+"?user=root&password=lejla");
        conForms.getStringInput("connection.jdbcDriver").setValue("com.mysql.jdbc.Driver");
        conForms.getStringInput("connection.username").setValue("root");
        conForms.getStringInput("connection.password").setValue("lejla");

        frameworkForms.getIntegerInput("security.maxConnections").setValue(0);

        Status status  = client.createConnection(newCon);
        if(status.canProceed()) {
            System.out.println("Created. New Connection ID : " +newCon.getPersistenceId());
        } else {
            System.out.println("Check for status and forms error ");
        }


        //Creating dummy job object
        MJob newjob = client.newJob(newCon.getPersistenceId(), org.apache.sqoop.model.MJob.Type.IMPORT);
        MJobForms connectorForm = newjob.getConnectorPart();
        MJobForms frameworkForm = newjob.getFrameworkPart();

        newjob.setName("ImportJob");


        //connectorForm.getStringInput("table.columns").setValue("id,mcc");
        String query_string="select " + typeOfDate +" from " + shema + "." + tableName +" where ${CONDITIONS}";
        connectorForm.getStringInput("table.sql").setValue(query_string);
        connectorForm.getStringInput("table.partitionColumn").setValue("datum");
        /*if(tableName.contains("result_summary_"))
            connectorForm.getStringInput("table.partitionColumn").setValue("result_summary_id");
        else
            connectorForm.getStringInput("table.partitionColumn").setValue("id");*/
        //Set boundary value only if required
        //connectorForm.getStringInput("table.boundaryQuery").setValue("");


        //Output configurations
        frameworkForm.getEnumInput("output.storageType").setValue("HDFS");
        frameworkForm.getEnumInput("output.outputFormat").setValue("TEXT_FILE");//Other option: SEQUENCE_FILE
        frameworkForm.getStringInput("output.outputDirectory").setValue(outputDir);


        //Job resources
        //frameworkForm.getIntegerInput("throttling.extractors").setValue(1);
        //frameworkForm.getIntegerInput("throttling.loaders").setValue(1);

        status = client.createJob(newjob);
        if(status.canProceed()) {
            System.out.println("New Job ID: "+ newjob.getPersistenceId());
        } else {
            System.out.println("Check for status and forms error ");
        }

        printMessage(newjob.getConnectorPart().getForms());
        printMessage(newjob.getFrameworkPart().getForms());


        try {
            client.startSubmission(newjob.getPersistenceId(),new SubmissionCallback() {

                @Override
                public void updated(MSubmission submission) {

                    if(submission.getStatus().isRunning() && submission.getProgress() != -1) {
                        System.out.println("Progress : " + String.format("%.2f %%", submission.getProgress() * 100));
                    }

                }

                @Override
                public void submitted(MSubmission submission) {

                    if(submission.getExceptionInfo() != null) {
                        System.out.println("Exception info : " +submission.getExceptionInfo());
                    }

                    System.out.println("Status : " + submission.getStatus());

                    if(submission.getStatus().isRunning() && submission.getProgress() != -1) {
                        System.out.println("Progress : " + String.format("%.2f %%", submission.getProgress() * 100));
                    }

                }

                @Override
                public void finished(MSubmission submission) {

                    System.out.println("Hadoop job id :" + submission.getExternalId());
                    System.out.println("Job link : " + submission.getExternalLink());

                    Counters counters = submission.getCounters();
                    if(counters != null) {
                        System.out.println("Counters:");
                        for(CounterGroup group : counters) {
                            System.out.print("\t");
                            System.out.println(group.getName());
                            for(Counter counter : group) {
                                System.out.print("\t\t");
                                System.out.print(counter.getName());
                                System.out.print(": ");
                                System.out.println(counter.getValue());
                            }
                        }
                    }

                    //client.stopSubmission(1);

                }
            },10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        client.deleteJob(newjob.getPersistenceId());
        client.deleteConnection(newCon.getPersistenceId());
    }

    private static void sqoopExport(String tableName,String shema,String inputDir){
        String url = "http://localhost:12000/sqoop/";
        SqoopClient client = new SqoopClient(url);

        MConnection newCon = client.newConnection(1);
        //Get connection and framework forms. Set name for connection
        MConnectionForms conForms = newCon.getConnectorPart();
        MConnectionForms frameworkForms = newCon.getFrameworkPart();
        newCon.setName("MyExportConnection");

        //Set connection forms values
        conForms.getStringInput("connection.connectionString").setValue("jdbc:mysql://localhost:3306/"+shema+"?user=root&password=lejla");
        conForms.getStringInput("connection.jdbcDriver").setValue("com.mysql.jdbc.Driver");
        conForms.getStringInput("connection.username").setValue("root");
        conForms.getStringInput("connection.password").setValue("lejla");

        frameworkForms.getIntegerInput("security.maxConnections").setValue(0);

        Status status  = client.createConnection(newCon);
        if(status.canProceed()) {
            System.out.println("Created. New Connection ID : " +newCon.getPersistenceId());
        } else {
            System.out.println("Check for status and forms error ");
        }

        MJob newjob = client.newJob(1, org.apache.sqoop.model.MJob.Type.EXPORT);
        MJobForms connectorForm = newjob.getConnectorPart();
        MJobForms frameworkForm = newjob.getFrameworkPart();

        newjob.setName("ExportJob");
        //Database configuration
        connectorForm.getStringInput("table.schemaName").setValue(shema);
        //Input either table name or sql
        connectorForm.getStringInput("table.tableName").setValue(tableName);
        //connectorForm.getStringInput("table.sql").setValue("select id,name from table where ${CONDITIONS}");

        //Input configurations
        frameworkForm.getStringInput("input.inputDirectory").setValue(inputDir);

        //Job resources
        //frameworkForm.getIntegerInput("throttling.extractors").setValue(1);
        //frameworkForm.getIntegerInput("throttling.loaders").setValue(1);

        status = client.createJob(newjob);
        if(status.canProceed()) {
            System.out.println("New Job ID: "+ newjob.getPersistenceId());
        } else {
            System.out.println("Check for status and forms error ");
        }

        //Print errors or warnings
        printMessage(newjob.getConnectorPart().getForms());
        printMessage(newjob.getFrameworkPart().getForms());

        try {
            client.startSubmission(newjob.getPersistenceId(),new SubmissionCallback() {

                @Override
                public void updated(MSubmission submission) {

                    if(submission.getStatus().isRunning() && submission.getProgress() != -1) {
                        System.out.println("Progress : " + String.format("%.2f %%", submission.getProgress() * 100));
                    }

                }

                @Override
                public void submitted(MSubmission submission) {

                    if(submission.getExceptionInfo() != null) {
                        System.out.println("Exception info : " +submission.getExceptionInfo());
                    }

                    System.out.println("Status : " + submission.getStatus());

                    if(submission.getStatus().isRunning() && submission.getProgress() != -1) {
                        System.out.println("Progress : " + String.format("%.2f %%", submission.getProgress() * 100));
                    }

                }

                @Override
                public void finished(MSubmission submission) {

                    System.out.println("Hadoop job id :" + submission.getExternalId());
                    System.out.println("Job link : " + submission.getExternalLink());

                    Counters counters = submission.getCounters();
                    if(counters != null) {
                        System.out.println("Counters:");
                        for(CounterGroup group : counters) {
                            System.out.print("\t");
                            System.out.println(group.getName());
                            for(Counter counter : group) {
                                System.out.print("\t\t");
                                System.out.print(counter.getName());
                                System.out.print(": ");
                                System.out.println(counter.getValue());
                            }
                        }
                    }

                    //client.stopSubmission(1);

                }
            },10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        client.deleteJob(newjob.getPersistenceId());
        client.deleteConnection(newCon.getPersistenceId());
    }

    private static void printMessage(List<MForm> formList) {
        for(MForm form : formList) {
            List<MInput<?>> inputlist = form.getInputs();
            if (form.getValidationMessage() != null) {
                System.out.println("Form message: " + form.getValidationMessage());
            }
            for (MInput minput : inputlist) {
                if (minput.getValidationStatus() == Status.ACCEPTABLE) {
                    System.out.println("Warning:" + minput.getValidationMessage());
                } else if (minput.getValidationStatus() == Status.UNACCEPTABLE) {
                    System.out.println("Error:" + minput.getValidationMessage());
                }
            }
        }
    }

}

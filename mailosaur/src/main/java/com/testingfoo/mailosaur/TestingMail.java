package com.testingfoo.mailosaur;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.mailosaur.MailosaurClient;
import com.mailosaur.MailosaurException;
import com.mailosaur.models.SearchCriteria;
import com.mailosaur.models.MessageListResult;
import com.mailosaur.models.MessageSummary;
//import com.mailosaur.models.Message;

public class TestingMail {
	
	private static MailosaurClient client;
	private static List<MessageSummary> emails;
	private static Properties props;
	private static Session session;
	public static Properties sessionProperties;
	public static Properties sessionDetails; 
	public static FileInputStream fis;
	
	private String serverID;
	private String apiKey;
	
	@BeforeClass
	public void setUp() throws IOException {
		
		//Loads the sessionProperties file
		sessionProperties=new Properties();
		fis=new FileInputStream(System.getProperty("user.dir")+"//src//main//java//properties//Sessionproperties.properties");
		sessionProperties.load(fis);
		
		//Loads the sessionDetails file
		sessionDetails=new Properties();
		fis=new FileInputStream(System.getProperty("user.dir")+"//src//main//java//properties//Serverdetails.properties");
		sessionDetails.load(fis);
		
		//creates the session to send mails to SMTP server
		createSession();
		
		//Setting the ServerID and api Key needed for mail validations
		serverID=sessionDetails.getProperty("serverID");
		apiKey=sessionDetails.getProperty("apiKey");
	
	}
	
	@BeforeMethod
	public void addMails() {
		
		//Sending 5 mails needed for the test from particular sender
		sendMail("noreply@company.com","some.recipient@example.com","Email test","Something profound");
		sendMail("noreply@company.com","some.recipient@example.com","Email test2","Something profound");
		sendMail("noreply@company.com","some.recipient@example.com","Email test3","Something profound");
		sendMail("noreply@company.com","some.recipient@example.com","Email test4","Something profou");
		sendMail("noreply@company.com","some.recipient@example.com","Email test5","Something profo");

		//Sending 5 mails needed for the test from different sender
		sendMail("noreply@company.com","rick.segall@example.com","Email test","Something profound");
		sendMail("noreply@company.com","tom.rose@example.com","Email test2","Something profound");
		sendMail("noreply@company.com","Steve.williams@example.com","Email test3","Something profound");
		sendMail("noreply@company.com","Gaurav.Singh@example.com","Email test4","Something profound");
		sendMail("noreply@company.com","Uday.Seshadri@example.com","Email test5","Something profo");
	}
	
	@Test
	public void test() throws IOException, MailosaurException {
		
		//Instantiating Mailosaur client object
		client = new MailosaurClient(apiKey);
		
		//Providing the particular sender mail id for test
		String userMailID="some.recipient@example.com";
		
		//Providing the Specific string details to search in the mailbox
		String MessageSummaryBody="Something profound";
		
		System.out.println("Validating the test for more than 50% of mails with particular String : "+ userMailID);
		
		//Instantiating Search Criteria object to search for particular sender.
		SearchCriteria criteria = new SearchCriteria();
		
		//creating the list of mails from particular sender
		criteria.withSentTo(userMailID);
		MessageListResult result=client.messages().search(serverID, criteria);
		emails=result.items();
		
		int countOfParticularString=0;
		int countOfAllmails=emails.size();
       
		//Identifying the existence of particular string in the mailbox 
		for (MessageSummary email : emails) {

			String MessageSummary = email.summary();

			if(MessageSummary.contains(MessageSummaryBody)) {
				
				++countOfParticularString;
			}

		}
		
		//Calculating the percentage of mails
		float percentageOfTextInAllMails=((float)countOfParticularString/(float)countOfAllmails)*100;
		
		System.out.println("Percentage:"+ percentageOfTextInAllMails);
		
		//Test to validate Atlease 50% of mail from specific user has specified string
		Assert.assertTrue(percentageOfTextInAllMails>=50.00, "Atlease 50% of mail from specific user has specified string");
		
		
	}
	
	@AfterMethod
	public void tearDown() throws MailosaurException {
	
		client = new MailosaurClient(apiKey);
		client.messages().deleteAll(serverID);        
		
	}
	
	@AfterClass
	public void cleanUp() throws IOException {
		
		fis.close();
	}
	
	
	/**
	* 
	* Create Session Method creates a session inorder to perform the Mailosaur activities.
	* We are using a Properties file "sessionProperties" to pass multiple configuration details
	* related to SMTP server.
	* @author  udi08s
	* @version 1.0
	* @since   2019-07-19 
	* 
	*/
	public void createSession() throws IOException {
		
		props=new Properties();

		props.put("mail.smtp.host", sessionProperties.getProperty("host"));
		props.put("mail.smtp.socketFactory.port", sessionProperties.getProperty("socketFactoryPort")); 
		props.put("mail.smtp.auth",sessionProperties.getProperty("auth")); 
		props.put("mail.smtp.port", sessionProperties.getProperty("port"));
		
		session = Session.getDefaultInstance(props,
		  new javax.mail.Authenticator() {
		    protected PasswordAuthentication getPasswordAuthentication() {
		        return new PasswordAuthentication("rbye4ksn",
		          "P1urnfWK");
		    }    
		  });
	}

	
	
	/**
	* 
	* SendMail Method sends email to the created SMTP 
	* This method accepts 4 parameters
	* 1. fromAddress
	* 2. toAddress
	* 3. mailSubject
	* 4. mailBodySummary
	* 
	* If all configurations are successful, mail will be send to the SMTP server.
	* 
	* @author  udi08s
	* @version 1.0
	* @since   2019-07-19 
	* 
	*/
	public void sendMail(String fromAddress,String toAddress,String mailSubject,String mailSummary) {
		
		
		try {
		    MimeMessage message = new MimeMessage(session);
		    message.setFrom(new InternetAddress(fromAddress));
		    message.addRecipient(Message.RecipientType.TO,
		      new InternetAddress(toAddress));
		    message.setSubject(mailSubject);
		    message.setText(mailSummary);
		    
		    Transport.send(message);

		    System.out.println("Message sent successfully:" + toAddress);   
		} catch (MessagingException e) {
		    throw new RuntimeException(e);
		}
	}
}

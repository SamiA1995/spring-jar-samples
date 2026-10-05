package com.example.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@SpringBootApplication
@RestController
public class DemoApplication {
    public static void main(String[] args) {
      SpringApplication.run(DemoApplication.class, args);
    }

	@GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "John Doe") String name, 
						@RequestParam(value = "age", defaultValue = "30") String age) {
		System.out.println(name + " " + age);
		return name + " " + age;
    }

	@GetMapping("/helloJson")
    public String helloJson(@RequestParam(value = "name", defaultValue = "Max Verstappen") String name, 
							@RequestParam(value = "age", defaultValue = "28") String age) {
		System.out.println("{\"name\":\"" + name + " Doyle\"}");
		String result = "{\"name\":\"" + name + " Doyle\"";
		result += ",\"age\":\"" + age + "\"";
		result += "}";
		System.out.println(result);
		return result;
    }

	@GetMapping("/stats")
	public String stats() throws FileNotFoundException {
		String date = "04.10";
		String environment = "Test";

		String[][] servers_queue_managers = new String[1][3];

		initializeServers(servers_queue_managers, environment);
		ArrayList<String> queue_manager_queue_count = scanAMQSMON(date, servers_queue_managers, environment);

		int currentDepth = 0;
		for(int i = 0; i < queue_manager_queue_count.size(); i++) {
			String[] result = queue_manager_queue_count.get(i).split(" ");
			currentDepth += Integer.parseInt(result[2]);
		}
		return ("{\"put_count\":\"" + currentDepth + "\"}");
	}

	private ArrayList<String> scanAMQSMON(String date, String[][] servers_queue_managers, String environment) throws FileNotFoundException {
		ArrayList<String> queue_manager_queue_count = new ArrayList();

		for(int i = 0; i < servers_queue_managers.length; i++) {
			String server = servers_queue_managers[i][0];
			String queue_manager_start = servers_queue_managers[i][1];
			String queue_manager_base = queue_manager_start.substring(0, queue_manager_start.length()-1);
			int queue_manager_number = Integer.parseInt(queue_manager_start.substring(queue_manager_start.length()-1, queue_manager_start.length()));
			for(int j = 0; j < Integer.parseInt(servers_queue_managers[i][2]); j++) {
				String queue_manager = queue_manager_base + (queue_manager_number+j);
				Scanner scanner = new Scanner(new File("/mqlocal/home/mqm/mq_stats/EMBM06D7_stats_04.10.txt"));
				//Scanner scanner = new Scanner(new File("C:\\Shared\\Career\\Software Engineering\\Projects\\spring-jar-samples\\demo\\EMBM06D7_stats_04.10.txt"));
				while(scanner.hasNextLine()) {
					String line = scanner.nextLine();
					Pattern pattern = Pattern.compile("DEV.QUEUE.9", Pattern.CASE_INSENSITIVE);
					Matcher matcher = pattern.matcher(line);
					boolean match_found = matcher.find();
					if(match_found) {
						String queue = parseQueue(line);
						boolean find_put_count = false;
						while(!find_put_count) {
							line = scanner.nextLine();
							Pattern pattern_2 = Pattern.compile("Put1Count:", Pattern.CASE_INSENSITIVE);
							Matcher matcher_2 = pattern_2.matcher(line);
							boolean match_found_2 = matcher_2.find();
							if(match_found_2) {
								String count = parseCount(line);
								find_put_count = true;
								if(!count.equals("0")) {
									queue_manager_queue_count.add(queue_manager + " " + queue + " " + count);
								}
							}
						}
					}
				}
				scanner.close();
			}
		}
		return queue_manager_queue_count;
	}

	private String parseQueue(String text) {
		String[] splitted_text = text.split("'");
		return splitted_text[1];
	}

	private String parseCount(String text) {
		String[] splitted_text = text.split(",");
		return splitted_text[1].substring(1, splitted_text[1].length()-1);
	}

	private void initializeServers(String[][] servers_queue_managers, String environment) {
		if(environment.equals("Test")) {
			servers_queue_managers[0][0] = "bld02752001";

			servers_queue_managers[0][1] = "EMBM06D7";
			servers_queue_managers[0][2] = "1";
		}
	}
}
package sandbox2.mds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Test3 {

	public static void main(String[] args) {
		System.out.println("Test 3: command line arguments");
		System.out.println("N. args: " + args.length);
		args[0] = args[0] + "#1";
		for (String s : args) {
			System.out.println("arg: " + s);
		}
		
		List<String> l = new ArrayList<>();
		l.addAll(Arrays.asList(args));
		l.add("pippo2");
		
		List<Integer> nl = l.stream()
							.filter(s -> s.toUpperCase().startsWith("P")) // lambda function
							.map(s -> 
								{ 
									System.out.println(s);
									return s.length();
								})
							.collect(Collectors.toList());
		nl.stream()
		  .forEach(n -> System.out.println(n));
		 
		
	}

}

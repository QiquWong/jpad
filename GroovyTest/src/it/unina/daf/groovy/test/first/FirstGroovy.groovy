package it.unina.daf.groovy.test.first

import groovy.transform.ToString
import groovy.transform.builder.Builder
import groovy.text.SimpleTemplateEngine

// www.vogella.com/tutorials/Groovy/article.html

class FirstGroovy {

	@Builder
	@ToString(includeNames=true)
	class Task {
		String summary
		//String description
		int duration
	}

	static void main(def args){
		def mylist= [1,2,"Lars","4"]
		mylist.each{ println it }

		println '------------'

		String templateText = '''Project report:

We have currently ${tasks.size} number of items with a total duration of $duration.
<% tasks.each { %>- $it.summary
<% } %>

'''
		def list = [
			Task.builder().
				summary("Learn Groovy").
				duration(4).build(),
			Task.builder().
				summary("Agodemar").
				duration(2).build()
			]
		def totalDuration = 0
		list.each{totalDuration += it.duration}
		def engine = new SimpleTemplateEngine()
		def template = engine.createTemplate(templateText)
		def binding = [
			duration: "$totalDuration",
			tasks: list]

		println template.make(binding).toString()

	}

}

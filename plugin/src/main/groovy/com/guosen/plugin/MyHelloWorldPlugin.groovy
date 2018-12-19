package com.guosen.plugin

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class MyHelloWorldPlugin implements Plugin<Project> {

     static final String LINT_NAME = 'LintCleaner';
     static final String EXTENSION_NAME = 'lintCleaner';
     static final String ANDROID_LINT_TASK = 'lint'

    void apply(Project project) {
        System.out.println("========================");
        System.out.println("start gradle plugin!");
        System.out.println("========================");

        project.extensions.create(EXTENSION_NAME, HugoExtension,project);

      Task cleanTask = project.tasks.create(CleanUnUsedResTask.TASK_NAME,CleanUnUsedResTask)
        cleanTask.run()
       // project.task(CleanUnUsedResTask.TASK_NAME, type: CleanUnUsedResTask, dependsOn: ANDROID_LINT_TASK)

//        project.tasks.create('pluginTest') {
//            doLast {
//                println "This is Test"
//            }
//        }
        project.afterEvaluate {
            HugoExtension helloModel = project.getExtensions().getByType(HugoExtension);

            if (helloModel.getOutFile()==null){
                //throw new GradleException("out file 不能为空")
            }
        }
    }
}

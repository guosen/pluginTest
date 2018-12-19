package com.guosen.plugin

import org.gradle.api.Project

class HugoExtension {
    def message = 'Hugo'
    def outFile ;

    String lintXmlPath;
    String lintOutPutPath;


    List<String> exclude = []

    boolean ignoreResFiles = false

    def setTest(String str) {
        System.out.println("Hugo:" + str);
    }

    public HugoExtension(Project project){
        lintXmlPath = "$project.buildDir/app/reports/lint-results.xml"
        lintOutPutPath = "$project.buildDir/app/reports/lintCleanerLog.txt"
        ignoreResFiles = false;

    }

    @Override
    String toString() {
        return "配置值+++++++:\n\tlintXmlPath:" + lintXmlPath + "\n" +
                "outputPath:" + lintOutPutPath + "\n"
    }
}
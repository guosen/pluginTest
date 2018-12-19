package com.guosen.plugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.NodeList
class CleanUnUsedResTask extends DefaultTask {


    static String TASK_NAME = "cleanUnusedRes";
    final String UNUSED_RESOURCES_ID = "UnusedResources"
    final String OUT_XML_TAG = "issue"
    HashSet<String> mFilePath = new HashMap<>()
    StringBuilder mSbRemoveLog = new StringBuilder()
    StringBuilder mSbKeepLog = new StringBuilder()

    final String LINE_XML_TAG = "line"
    final String LOCATION_XML_TAG = "location"
    final String FILE_PATH_XML_TAG = "file"
    final String ID_XML_TAG = "id"
    List<String> excludes
    final String ARRAY_XML_TAG = "array"
    final String WRITER_ENCODING = "UTF-8"
    final String LINE_SEPARATOR = System.getProperty("line.separator")
    boolean ignoreResFiles

    final Map<String, List<String>> filePathToLines = new HashMap<String, ArrayList<String>>()//String.xml
    CleanUnUsedResTask(){
        super()
        dependsOn "app:lint"
    }
    @TaskAction
    public void run() {
        def ext = project.extensions.findByName(MyHelloWorldPlugin.EXTENSION_NAME) as HugoExtension
        println ext.toString()
        def file = new File(ext.lintXmlPath);
        if (!file.exists()){
            println '找不到lint的xml文件，请检查路径是否正确！'
            return
        }
        new XmlSlurper().parse(file).'**'.findAll{
            node ->
                if (node.name() == OUT_XML_TAG && node.@id == UNUSED_RESOURCES_ID) {
                    mFilePath.add(node.location.@file)
                }
        }

        def num = mFilePath.size();
        if (num > 0){

            mSbRemoveLog.append("num:${num}\n");
            println(mSbRemoveLog)
            for (String path : mFilePath){
                println path
                removeResUnUsed(ext.lintOutPutPath)
            }
            writeToOutput(path)
        }else {
            print '不存在无用资源！'
        }

        //####删除String.xml#########
        def builderFactory = DocumentBuilderFactory.newInstance()
        Document lintDocument = builderFactory.newDocumentBuilder().parse(ext.lintXmlPath)

        NodeList issues = lintDocument.getElementsByTagName(OUT_XML_TAG)
        processIssues(issues)

        if (!getIgnoreResFiles()) {
            excludes = getExcludes()
            removeUnusedLinesInResFiles()
        }
    }

    public void cleanTask(){
        group = MyHelloWorldPlugin.LINT_NAME
        description = "Removes unused resources reported by Android lint task"
    }


    def removeResUnUsed(String path){
        if (isDelFile(path)){
            if (new File(path).delete()){
                mSbRemoveLog.append('\n\t 删除成功'+path);
            }else {
                mSbKeepLog.append('\n\t删除失败'+path)
            }
            println 'delete finish' + mSbRemoveLog
            println   'delete finish'  + mSbKeepLog
        }else {

            print 'not delete file not need delete'
        }
    }

    def isDelFile(String path){
        String dir = path;
        return (dir.contains('drawable') || dir.contains('mipmap') || dir.contains('menu'))
    }

    def writeToOutput(def path) {
        def f = new File(path)
        if (f.exists()) {
            f.delete()
        }
        new File(path).withPrintWriter { pw ->
            pw.write(mSbRemoveLog.toString())
            pw.write(mSbKeepLog.toString())
        }
    }

    void removeUnusedLinesInResFiles(){
        filePathToLines.each { filePath, unusedLines ->
            File sourceFile = new File(filePath)

            if (excludes!=null && excludes.contains(sourceFile.name)) {
                return
            }

            def sourceDir = sourceFile.getParentFile().toString()
            File tempFile = new File("${sourceDir}/${sourceFile.name}bak")

            tempFile.withWriter(WRITER_ENCODING) { writer ->
                int index = 1
                boolean removingArray = false
                sourceFile.eachLine { line ->

                    String lineNumber = Integer.toString(index)
                    if (unusedLines.contains(lineNumber) || removingArray) {
                        if (line.contains(ARRAY_XML_TAG)) {
                            removingArray = !removingArray
                        }
                    } else {
                        writer << line + LINE_SEPARATOR
                    }
                    index++
                }
            }

            sourceFile.setWritable(true)
            sourceFile.delete()
            if (tempFile.renameTo(sourceFile)) {
                printEntryRemovalCount(sourceFile, unusedLines.size())
            } else {
                tempFile.delete()
                println "Failed to remove entries from $sourceFile.name"
            }
        }

    }

    /** 移除元素. */
    void processLocation(Element location) {
        String line = location.getAttribute(LINE_XML_TAG)
        String filePath = location.getAttribute(FILE_PATH_XML_TAG)

        if (line.empty) {
            File file = new File(filePath)
            file.delete()
            println "Removed $file.name"
        } else {
            List<String> lineNumbers = filePathToLines.get(filePath)
            lineNumbers = lineNumbers ? lineNumbers : new ArrayList<String>()
            lineNumbers.add(line)
            filePathToLines.put(filePath, lineNumbers)
        }
    }

    void processIssues(NodeList issues) {
        issues.each {
            Element issue = it as  Element

            if (issue.getAttribute(ID_XML_TAG).equals(UNUSED_RESOURCES_ID)) {
                NodeList locations = issue.getElementsByTagName(LOCATION_XML_TAG)

                if (locations.length == 1) {
                    processLocation(locations.item(0) as  Element)
                } else {
                    locations.each {
                        processLocation(it as Element)
                    }
                }
            }
        }
    }
    static void printEntryRemovalCount(File file, int count) {
        println "Removed $count ${count == 1 ? "entry" : "entries"} from $file.name"
    }
}
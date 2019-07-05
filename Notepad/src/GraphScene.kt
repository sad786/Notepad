package sample
import javafx.application.Application
import javafx.scene.Scene
import com.jfoenix.controls.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.Event
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.control.*
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import javafx.stage.*
import java.io.*

class GraphScene:Application()
{
        private val tabPane  = TabPane()
        private val boLabel  = Label("")
        private val chooser  = FileChooser()
        private val saveList = HashMap<String,Boolean>()
        private val fileList = HashMap<String,String?>()

        private var st:Stage?=null

        init {
           // tabPane.style = "-fx-background-color:cyan;"
            tabPane.tabClosingPolicy = TabPane.TabClosingPolicy.ALL_TABS
            setPath(File("."))
            chooser.extensionFilters.addAll(getExtension())
        }

        private fun setPath(pathName: File)
        {
            chooser.initialDirectory = pathName
        }
        //Return the Current selected BorderPane
        private fun getBorder(): BorderPane
        {
            var node = this.getTab().content
            return node as BorderPane
        }

        // it will return TextArea
        private fun getPad():JFXTextArea
        {
            val node = this.getBorder()
            return node.center as JFXTextArea
        }

    // it will return VBox to called
        private fun getVBox():VBox
        {
            val pane = this.getBorder()
            return pane.left as VBox
        }
        //Returns the current selected Tab
        private fun getTab(): Tab = tabPane.selectionModel.selectedItem

        private fun createMenu(): MenuBar
        {
            val mb = MenuBar()
            // creating Menus
            val fileMenu = Menu("File")
            val editMenu = Menu("Edit")
            val viewMenu = Menu("View")

            //creating MenuItem
            val open = MenuItem("_Open")
            open.accelerator = this.mnemonic("O")
            open.setOnAction { this.openFile() }
            val save = MenuItem("_Save")
            save.accelerator = this.mnemonic("S")
            save.setOnAction { this.saveFile() }

            val new = MenuItem("_New")
            new.accelerator = this.mnemonic("N")
            new.setOnAction { this.createTab() }
            val exit = MenuItem("Exit")
            exit.setOnAction { System.exit(1) }
            val cut = MenuItem("Cut")
            cut.setOnAction { getPad().cut() }
            val copy = MenuItem("Copy")
            copy.setOnAction { getPad().copy() }
            val paste = MenuItem("Paste")
            paste.setOnAction { getPad().paste() }
            val selectAll = MenuItem("SelectAll")
            selectAll.setOnAction { getPad().selectAll() }

            //adding items to the menus
            fileMenu.items.addAll(new, open, save, exit)
            editMenu.items.addAll(selectAll, copy, cut, paste)
            mb.menus.addAll(fileMenu, editMenu, viewMenu)

            return mb
        }

    // this function sets the mnemonic to the menus or buttons
    private fun mnemonic(key:String):KeyCombination
    {
        val code = KeyCode.getKeyCode(key)
        return KeyCodeCombination(code, KeyCombination.CONTROL_DOWN)
    }

    // this function creates Tab for using
    private fun createTab()
    {
        // creating the text area and styling
        val textArea = JFXTextArea()
        textArea.prefRowCount = 1
        textArea.accessibleText = "1"
        textArea.style = "-fx-background-color:white;"
        textArea.font = Font.font(15.0)
        textArea.focusColor = Color.GREEN
        textArea.setOnKeyTyped { this.keyChanged() }
       // textArea.prefRowCountProperty().addListener { _ -> println("Pref Row Count Listener") }

        //setting the style for the button
        val btn = JFXButton("+")
        btn.font = Font.font(null,FontWeight.BOLD, 18.0)
        btn.isVisible = false
        btn.isDisable = true
        btn.setOnAction { this.createTab() }

        //creating BorderPane
        val pane = BorderPane()
        pane.center = textArea

        // creating tab and styling
        val tab = Tab("New${tabPane.tabs.size+1}  ")
        tab.graphic = btn
        tab.setOnSelectionChanged { this.change()}
       // tab.setOnClosed{ this.closing() }
        tab.setOnCloseRequest {e -> this.close(e) }
        tab.style = ""
        tab.content = pane

        //adding the tab to the tabPane
        tabPane.tabs.add(tab)
        tabPane.selectionModel.select(tab)  // selecting this tab
        this.saveList(tab.text,null,false)    // saving the list
    }

    private fun showDialog()
    {

        println("Entered inside")
        val dialog = Stage()
        val lb = Label("Do You Want to Save Your changes")
        lb.font = Font.font("${FontWeight.BOLD}")
        val btn1 = JFXButton("Yes")
        btn1.prefHeight = 25.0
        btn1.prefWidth = 70.0
        btn1.buttonType = JFXButton.ButtonType.RAISED
        btn1.style = "-fx-background-color:#2E9AFE; -fx-text-fill:white;"

        val btn2 = JFXButton("No")
        btn2.style = "-fx-background-color:#2E9AFE; -fx-text-fill:white;"
        btn2.prefWidth = 70.0
        btn2.prefHeight = 25.0
        btn2.buttonType = JFXButton.ButtonType.RAISED
        val hbox = HBox(10.0)
        hbox.children.addAll(btn1,btn2)
        val vbox = BorderPane()
        vbox.center = lb
        vbox.bottom = hbox
        hbox.alignment = Pos.CENTER
        hbox.padding = Insets(10.0)

        btn1.setOnAction {
            dialog.close()
            this.saveFile()
        }
        btn2.setOnAction{
            dialog.close()
        }
        val sc = Scene(vbox,400.0,100.0)
        dialog.scene = sc
        dialog.initOwner(st)
        dialog.initModality(Modality.APPLICATION_MODAL)
        dialog.initStyle(StageStyle.UTILITY)
        dialog.showAndWait()
    }

    private fun close(e:Event)
    {
            println("before closing")
            val tab = e.source as Tab
            tabPane.selectionModel.select(tab)
            e.consume()

            if(tab.text.contains("*")) {
                showDialog()
            }
            this.remove()
        }

    private fun remove()
    {
        val tab = getTab()
        val str = removePre(tab.text)
        fileList.remove(str)  // to remove the entry from file list
        saveList.remove(str) // to remove the entry from save list
       // println("After Removing FileList is  $fileList \n SaveList is $saveList")
        tabPane.tabs.remove(tab)
        closing()
    }

    // this function will handle the tab closing
    private fun closing()
    {
        if(tabPane.tabs.size==0) // when tab remain one it will not allow that tab to close
            createTab()
    }



    //This function will handle the changes in Text Area Data
    private fun keyChanged()
    {
        val curTab = getTab()
        if(!curTab.text.contains("*"))
            curTab.text = "*${curTab.text}"

    }



    // to save the each tap entry to the list for further processing
    private fun saveList(name:String,fileName:String?=null,status:Boolean =false)
    {
        //val key = getTab().text.toString()
        fileList[name] = fileName
        saveList[name] = status
       //println("After Saving FileList is $fileList \n SaveList is $saveList")

       // println("Save List is $saveList")
       // println("File List is $fileList")
    }
    private fun removePre(text:String) = if(text.contains("*"))
                text.removePrefix("*").trim()
            else
                text

     private fun change()
     {
        val tab = getTab()
         val text = tab.text
        val title:String? = fileList[removePre(text)]
        st?.title = "Notepad     ${title ?: tab.text}"
        var btn = tab.graphic as JFXButton

        if(tab.isSelected) {
            btn.isDisable = false
            btn.isVisible = true
        }
        else {
            btn.isDisable = true
            btn.isVisible = false
        }

     }

    private fun openFile()
    {
        chooser.title = "Choose File"
        val list:List<File>? = chooser.showOpenMultipleDialog(st)
        if(list!=null)
        {
            try
            {
                var br:BufferedReader?
                var str:String?
                for(file in list) {
                    if(fileList[file.name]!=file.absolutePath)
                    {
                        this.createTab()
                        br = BufferedReader(FileReader(file))
                        var text = getPad()
                        str = br.readLine()
                        while (str != null) {
                            text.text = text.text + str + "\n"
                            str = br.readLine()
                        }
                        br.close()
                        this.remove()   //it will remove the redundant tab
                        getTab().text = file.name
                        st?.title = "Notepad ${file.absolutePath}"
                        this.saveList(name = file.name,fileName = file.absolutePath, status = true)
                    }else
                        print("already opened file name ${file.name}")
                }

            }catch (ex: IOException){println(ex)}
        }
        else
            println("I am Open Null")
    }

    // for saving the file into the hard-disk
    private fun saveFile()
    {
        val tab = getTab()
        val key:String = if(tab.text.contains("*"))
                            tab.text.removePrefix("*").trim()
                        else
                            tab.text.trim()

        val b:Boolean? = saveList[key]

        println("$b = $key")

        if(b==true)
        {
            tab.style = "-fx-font-weight:normal;"
            val file = fileList[key]
            val text = this.getPad().text
            val writer = PrintWriter(file)
            writer.write(text)
            writer.flush()
            writer.close()
            tab.text = key

        }else
        {
            chooser.title = "Save File"
            chooser.initialFileName = "$key"
            val file:File? = chooser.showSaveDialog(st)
            if(file!=null)
            {
                val writer = PrintWriter(file)
                val data = this.getPad().text
                writer.write(data)
                writer.flush()
                writer.close()
                st?.title = "Notepad   ${file.absolutePath}"
                tab.text = file.name
                this.saveList(file.name,file.absolutePath,true)
                tab.style = "-fx-font-weight:normal;"

                val path = File(file.absolutePath.removeSuffix(file.name))
                setPath(path)
            }
        }
    }

    private fun getExtension():ObservableList<FileChooser.ExtensionFilter> = FXCollections.observableArrayList(
                                    FileChooser.ExtensionFilter("All Files","*.*"),
                                    FileChooser.ExtensionFilter("Text File","*.txt"),
                                    FileChooser.ExtensionFilter("Java File","*.java"),
                                    FileChooser.ExtensionFilter("C File","*.c"),
                                    FileChooser.ExtensionFilter("Python File","*.py"),
                                    FileChooser.ExtensionFilter("PDF File ","*.pdf"))


    override fun start(stage:Stage)
    {
        this.st = stage
        this.createTab()
        val root = BorderPane()
        root.bottom = boLabel
        root.top = this.createMenu()
        root.center = tabPane
        val scene = Scene(root, 500.0, 500.0)
        stage.scene = scene
        stage.title = "Notepad"
        stage.show()
    }
}

fun main()
{
    Application.launch(GraphScene::class.java)
}
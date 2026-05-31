package de.ruu.app.jeeeraaah.frontend.ui.fx.generate;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import de.ruu.lib.gen.GeneratorException;
import de.ruu.lib.gen.java.bean.BeanGenerator;
import de.ruu.lib.gen.java.fx.bean.FXBeanGenerator;
import de.ruu.lib.gen.java.fx.bean.editor.FXBeanViewFXMLGenerator;
import de.ruu.lib.gen.java.fx.comp.GeneratorFXCompBundle;
import de.ruu.lib.gen.java.fx.tableview.GeneratorFXTableViewConfigurator;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class TypeGenerator
{
	public static void main(String[] args) throws IOException, GeneratorException
	{
//		Class<?> taskInterfaceType = Task.class;
//
//		generateJavaBeanClass                  (taskInterfaceType);
//		generateJavaFXBeanClass                (taskInterfaceType, TaskEntityDTO.class);
//		generateJavaFXBeanViewFXML             (taskInterfaceType);
//		generateJavaFXBeanViewComponentBundle  (taskInterfaceType);
//		generateJavaFXBeanEditorComponentBundle(taskInterfaceType);
//
//		Class<?> taskGroupInterfaceType = TaskGroup.class;
//
//		generateJavaBeanClass                  (taskGroupInterfaceType);
//		generateJavaFXBeanClass                (taskGroupInterfaceType, TaskGroupEntityDTO.class);
//		generateJavaFXBeanViewFXML             (taskGroupInterfaceType);
//		generateJavaFXBeanViewComponentBundle  (taskGroupInterfaceType);
//		generateJavaFXBeanEditorComponentBundle(taskGroupInterfaceType);
//		generateJavaFXCTableViewConfigurator   (TaskGroupEntityDTO.class);
//
//		generateJavaFXComponentMain();
//		generateJavaFXComponentHierarchies();
		generateJavaFXComponentGantt();
//		generateJavaFXComponentTaskGroupSelector();
//		generateJavaFXComponentTaskViewHierarchy();
//		generateJavaFXComponentTaskViewListDirectNeighbours();
//		generateJavaFXComponentTaskViewWithDirectNeighbours();
//		generateJavaFXComponentTaskViewDirectNeighbourSuper();
//		generateJavaFXComponentAddPredecessorChooseSuccessorBundle();

//		generateJavaFXComponentTaskGroup();
	}

	private static void generateJavaFXCTableViewConfigurator(Class<?> interfaceType) throws GeneratorException, IOException
	{
		log.debug("create java fx table view configurator based on interface " + interfaceType.getName());
		GeneratorFXTableViewConfigurator generatorFXTableViewConfigurator =
				new GeneratorFXTableViewConfigurator
						(
								"de.ruu.app.jeeeraaah.client.fx.taskgroup",
								"TableViewConfigurator",
								interfaceType
						);

		generatorFXTableViewConfigurator.run();
	}

	private static void generateJavaFXComponentMain() throws GeneratorException, IOException
	{
		log.debug("create java fx component bundle for main component of application jeeeraaah");
		GeneratorFXCompBundle jeeeraaahMainComponentBundleGenerator =
				new GeneratorFXCompBundle
						(
								"de.ruu.app.jeeeraaah.client.fx",
								"Main"
						);

		jeeeraaahMainComponentBundleGenerator.run();
	}

	private static void generateJavaFXComponentHierarchies() throws GeneratorException, IOException
	{
		log.debug("create java fx component bundle for hierarchies component of application jeeeraaah");
		GeneratorFXCompBundle jeeeraaahMainComponentBundleGenerator =
				new GeneratorFXCompBundle
						(
								"de.ruu.app.jeeeraaah.client.fx",
								"Hierarchies"
						);

		jeeeraaahMainComponentBundleGenerator.run();
	}

	private static void generateJavaFXComponentGantt() throws GeneratorException, IOException
	{
		log.debug("create java fx component bundle for gantt component of application jeeeraaah");
		GeneratorFXCompBundle jeeeraaahMainComponentBundleGenerator =
				new GeneratorFXCompBundle
						(
								"de.ruu.app.jeeeraaah.frontend.ui.fx.task.gantt",
								"Gantt"
						);

		jeeeraaahMainComponentBundleGenerator.run();
	}

	private static void generateJavaFXComponentTaskGroupSelector() throws IOException, GeneratorException
	{
		log.debug("create java fx component bundle for task group selector component of application jeeeraaah");
		GeneratorFXCompBundle jeeeraaahMainComponentBundleGenerator =
				new GeneratorFXCompBundle
						(
								"de.ruu.app.jeeeraaah.client.fx",
								"TaskGroupSelector"
						);

		jeeeraaahMainComponentBundleGenerator.run();
	}

	private static void generateJavaFXComponentTaskViewHierarchy() throws IOException, GeneratorException
	{
		log.debug("create java fx component bundle for task hierarchy view component of application jeeeraaah");
		GeneratorFXCompBundle jeeeraaahMainComponentBundleGenerator =
				new GeneratorFXCompBundle
						(
								"de.ruu.app.jeeeraaah.client.fx.task.view.hierarchy",
								"TaskHierarchyView"
						);

		jeeeraaahMainComponentBundleGenerator.run();
	}

	private static void generateJavaFXComponentTaskViewListDirectNeighbours() throws IOException, GeneratorException
	{
		log.debug("create java fx component bundle for list view of direct task neighbours component of application jeeeraaah");
		GeneratorFXCompBundle jeeeraaahMainComponentBundleGenerator =
				new GeneratorFXCompBundle
						(
								"de.ruu.app.jeeeraaah.client.fx.task.view.list.directneighbours",
								"TaskViewListDirectNeighbours"
						);

		jeeeraaahMainComponentBundleGenerator.run();
	}

	private static void generateJavaFXComponentTaskViewWithDirectNeighbours() throws IOException, GeneratorException
	{
		log.debug("create java fx component bundle for task view with direct task neighbours component of application jeeeraaah");
		GeneratorFXCompBundle jeeeraaahMainComponentBundleGenerator =
				new GeneratorFXCompBundle
						(
								"de.ruu.app.jeeeraaah.client.fx.task.view.directneighbours",
								"TaskViewWithDirectNeighbours"
						);

		jeeeraaahMainComponentBundleGenerator.run();
	}

	private static void generateJavaFXComponentTaskViewDirectNeighbourSuper() throws IOException, GeneratorException
	{
		log.debug("create java fx component bundle for task view direct task neighbour super component of application jeeeraaah");
		GeneratorFXCompBundle jeeeraaahMainComponentBundleGenerator =
				new GeneratorFXCompBundle
						(
								"de.ruu.app.jeeeraaah.client.fx.task.view.directneighbours",
								"TaskViewDirectNeighbourSuper"
						);

		jeeeraaahMainComponentBundleGenerator.run();
	}

	private static void generateJavaFXComponentTaskGroup() throws GeneratorException, IOException
	{
		log.debug("create java fx component bundle for task group component of application jeeeraaah");
		GeneratorFXCompBundle jeeeraaahMainComponentBundleGenerator =
				new GeneratorFXCompBundle
						(
								"de.ruu.app.jeeeraaah.client.fx.taskgroup",
								"TaskGroupManagement"
						);

		jeeeraaahMainComponentBundleGenerator.run();
	}

	private static void generateJavaFXBeanEditorComponentBundle(Class<?> interfaceType) throws GeneratorException, IOException
	{
		log.debug("create java fx component bundle for editor based on interface " + interfaceType.getName());
		GeneratorFXCompBundle fxBeanEditorComponentBundleGenerator =
				new GeneratorFXCompBundle
						(
								interfaceType.getPackageName() + ".editor",
								interfaceType.getSimpleName() + "Editor"
						);

		fxBeanEditorComponentBundleGenerator.run();
	}

	private static void generateJavaFXBeanViewComponentBundle(Class<?> interfaceType) throws GeneratorException, IOException
	{
		log.debug("create java fx component bundle for view based on interface " + interfaceType.getName());
		GeneratorFXCompBundle fxBeanViewComponentBundleGenerator =
				new GeneratorFXCompBundle
						(
								interfaceType.getPackageName() + ".view",
								interfaceType.getSimpleName() + "View"
						);

		fxBeanViewComponentBundleGenerator.run();
	}

	private static void generateJavaFXBeanViewFXML(Class<?> interfaceType) throws GeneratorException, IOException
	{
		log.debug("create .fxml view file for java fx bean class based on interface " + interfaceType.getName());
		FXBeanViewFXMLGenerator fxBeanViewFXMLGenerator =
				new FXBeanViewFXMLGenerator
						(
								interfaceType.getPackageName() + ".view",
								interfaceType.getSimpleName() + "View",
								new ClassFileImporter().importClass(interfaceType)
						);

		fxBeanViewFXMLGenerator.run();
	}

	private static void generateJavaFXBeanClass(Class<?> interfaceType, Class<?> dtoType) throws GeneratorException, IOException
	{
		log.debug("create java fx bean class based on interface " + interfaceType.getName());
		FXBeanGenerator fxBeanGenerator =
				new FXBeanGenerator
						(
								interfaceType.getPackageName(),
								interfaceType.getSimpleName() + "FXBean",
								new ClassFileImporter().importClass(interfaceType),
								new ClassFileImporter().importClass(dtoType)
						);

		fxBeanGenerator.run();
	}

	private static void generateJavaBeanClass(Class<?> interfaceType) throws GeneratorException, IOException
	{
		log.debug("create java bean class based on interface " + interfaceType.getName());
		BeanGenerator beanGenerator =
				new BeanGenerator
						(
								interfaceType.getPackageName(),
								interfaceType.getSimpleName() + "Bean",
								new ClassFileImporter().importClass(interfaceType)
						);

		beanGenerator.run();
	}

	private static void generateJavaFXComponentAddPredecessorChooseSuccessorBundle() throws GeneratorException, IOException
	{
		log.debug("create java fx component bundle for add predecessor - choose successor");
		GeneratorFXCompBundle fxBeanViewComponentBundleGenerator =
				new GeneratorFXCompBundle
						(
								"de.ruu.app.jeeeraaah.client.fx.task.view.hierarchy.predecessor.add",
								"TaskSelector"
						);

		fxBeanViewComponentBundleGenerator.run();
	}
}
package lu.way.jadx.flow.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import lu.way.jadx.flow.taints.FlowdroidField;
import lu.way.jadx.flow.taints.GraphSummary;
import lu.way.jadx.flow.taints.PathSegmentType;
import lu.way.jadx.flow.taints.TaintMethodSegment;
import lu.way.jadx.flow.taints.TaintPathSegment;
import lu.way.jadx.flow.taints.TaintPathSummarize;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.swing_viewer.ViewPanel;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import org.graphstream.ui.view.util.InteractiveElement;
import org.graphstream.ui.view.util.MouseManager;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.*;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

public class PathInfoVisualizeDialog extends JDialog implements ViewerListener, MouseManager {
	private JPanel contentPane;
	private JButton buttonGraph;
	private JButton buttonCancel;
	private JTabbedPane fieldsList;
	private JList methodsList;
	private JList statementsList;
	private JList list3;
	private JCheckBox checkAutoStatement;
	private JCheckBox checkCombineMethod;
	private JPanel graphPanel;
	private JSplitPane splitPane;
	private JCheckBox checkAlwaysTop;

	private TaintPathSummarize taintSummary;
	private final FlowGUIDelegate delegate;
	private MethodListRenderer methodListRenderer;
	private GraphSummary currentGraph;
	private Graph currentGraphHolder;

	public PathInfoVisualizeDialog(FlowGUIDelegate delegate, TaintPathSummarize taintSummary) {
		setContentPane(contentPane);
		setModal(false);
		getRootPane().setDefaultButton(buttonGraph);

		this.taintSummary = taintSummary;
		this.delegate = delegate;

		buttonGraph.addActionListener(e -> onOK());

//		buttonCancel.addActionListener(e -> onCancel());

		// call onCancel() when cross is clicked
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});

		// call onCancel() on ESCAPE
		contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		contentPane.registerKeyboardAction(e -> {
			fieldsList.setSelectedIndex(0);
		}, KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		contentPane.registerKeyboardAction(e -> {
			fieldsList.setSelectedIndex(1);
		}, KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		contentPane.registerKeyboardAction(e -> {
			if (fieldsList.isEnabledAt(2))
				fieldsList.setSelectedIndex(2);
		}, KeyStroke.getKeyStroke(KeyEvent.VK_F, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		contentPane.registerKeyboardAction(e -> {
			toggleMethod();
		}, KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		contentPane.registerKeyboardAction(e -> {
			moveOnList(false);
		}, KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		contentPane.registerKeyboardAction(e -> {
			moveOnList(true);
		}, KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		contentPane.registerKeyboardAction(e -> {
			gotoMethod();
		}, KeyStroke.getKeyStroke(KeyEvent.VK_G, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		fieldsList.setEnabledAt(2, false);

//		splitPane.setDividerLocation(splitPane.getMaximumDividerLocation());

		splitPane.setRightComponent(null);

		methodsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		methodListRenderer = new MethodListRenderer(null);
		methodsList.setCellRenderer(methodListRenderer);
		methodsList.addListSelectionListener(this::onMethodSelectionUpdate);
		methodsList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onMethodClickEvent(e);
			}
		});
		statementsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		statementsList.setCellRenderer(new MethodStatementListRenderer());
		statementsList.addListSelectionListener(this::onStatementSelectionUpdate);
		list3.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list3.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onFieldClickEvent(e);
			}
		});
		updateMethodList();
		pack();
		checkAlwaysTop.addActionListener(e -> {
			setAlwaysOnTop(checkAlwaysTop.isSelected());
		});
		setAlwaysOnTop(true);

		updateTabTitle(0, "<html>Method(<u>a</u>)</html>");
		updateTabTitle(1, "<html>Statement(<u>s</u>)</html>");
		updateTabTitle(2, "<html><font color=\"#808080\">Field(<u>f</u>)</font></html>");

	}

	private void updateTabTitle(int index, String title) {
		fieldsList.setTitleAt(index, title);
	}

	private void updateMethodList() {
		methodsList.removeAll();
		DefaultListModel<TaintMethodSegment> model = new DefaultListModel<>();
		model.addAll(taintSummary.methodMaps.values().stream().flatMap(Collection::stream)
				.sorted(Comparator.comparingInt(TaintMethodSegment::getId)).collect(Collectors.toList()));
		methodsList.setModel(model);
	}

	private int lastIndex = -1;
	private int lastStatement = -1;

	private void onMethodClickEvent(MouseEvent e) {
		if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
			gotoMethod();
		}
	}

	private void onFieldClickEvent(MouseEvent e) {
		if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
			gotoField();
		}
	}

	private void gotoMethod() {
		lastIndex = methodsList.getSelectedIndex();
		if (lastIndex < 0) return;
		TaintMethodSegment segment = (TaintMethodSegment) methodsList.getModel().getElementAt(lastIndex);
		delegate.gotoMethodReference(segment.getMethod());

		if (checkAutoStatement.isSelected())
			fieldsList.setSelectedIndex(1);
	}

	private void gotoField() {
		lastIndex = list3.getSelectedIndex();
		if (lastIndex < 0) return;
		FlowdroidField field = (FlowdroidField) list3.getModel().getElementAt(lastIndex);
		delegate.gotoFieldReference(field.getValue());
	}

	private void onStatementSelectionUpdate(ListSelectionEvent e) {
		if (statementsList.getSelectedIndex() != lastStatement) {
			lastStatement = statementsList.getSelectedIndex();
			list3.removeAll();
			if (lastStatement < 0 || lastIndex < 0) return;
			TaintMethodSegment segment = (TaintMethodSegment) methodsList.getModel().getElementAt(lastIndex);
			TaintPathSegment pathSegment = segment.getPaths().get(lastStatement);
			if (pathSegment.getSegmentType() == PathSegmentType.PATH && !Objects.requireNonNull(pathSegment.getAccessPath()).getSubfields().isEmpty()) {
				DefaultListModel<FlowdroidField> model = new DefaultListModel<>();
				model.addAll(pathSegment.getAccessPath().getSubfields());
				list3.setModel(model);
				fieldsList.setEnabledAt(2, true);
				updateTabTitle(2, "<html>Field(<u>f</u>)</html>");
			} else {
				fieldsList.setEnabledAt(2, false);
				updateTabTitle(2, "<html><font color=\"#808080\">Field(<u>f</u>)</font></html>");
			}
			repaint();
		}
	}

	private void onMethodSelectionUpdate(ListSelectionEvent e) {
		if (methodsList.getSelectedIndex() != lastIndex) {
			lastIndex = methodsList.getSelectedIndex();
			lastStatement = -1;
			if (lastIndex < 0) return;
			TaintMethodSegment segment = (TaintMethodSegment) methodsList.getModel().getElementAt(lastIndex);
			methodListRenderer.setHighlightMethod(segment.getMethod());
			statementsList.removeAll();
			DefaultListModel<TaintPathSegment> model = new DefaultListModel<>();
			model.addAll(segment.getPaths());
			statementsList.setModel(model);
			methodsList.repaint();

			if (currentGraphHolder != null) {
				currentGraph.updateSelectedMethod(currentGraphHolder, segment.getMethod());
			}

		}
	}

	private void toggleMethod() {
		int index = methodsList.getSelectedIndex();
		if (index > 0) {
			TaintMethodSegment segment = (TaintMethodSegment) methodsList.getModel().getElementAt(index);
			List<TaintMethodSegment> paths = taintSummary.methodMaps.get(segment.getMethod());
			int taintIndex = paths.indexOf(segment);
			taintIndex = (taintIndex + 1) % paths.size();
			methodsList.setSelectedValue(paths.get(taintIndex), true);
			setTitle("Index: " + taintIndex);
		}
	}

	private void moveOnList(boolean forward) {
		JList operatedList = null;
		switch (fieldsList.getSelectedIndex()) {
			case 0:
				operatedList = methodsList;
				break;
			case 1:
				operatedList = statementsList;
				break;
			case 2:
				operatedList = list3;
				break;
			default:
				return;
		}
		int index = operatedList.getSelectedIndex();
		int size = operatedList.getModel().getSize();
		if (index >= 0 && index < size) {
			if (forward)
				index += 1;
			else
				index -= 1;
			index = (index + size) % size;
			operatedList.setSelectedIndex(index);
		}
	}

	private void onOK() {
		if (currentGraphHolder != null) return;
		// add your code here
		SingleGraph graph = new SingleGraph("Test graph view");
		currentGraphHolder = graph;
//		graph.addNode("A");
//		graph.addNode("B");
//		graph.addNode("C");
//		graph.addEdge("AB", "A", "B");
//		graph.addEdge("AC", "B", "C");
		graph.setAttribute("ui.stylesheet", "node.source { fill-color: red; }\nnode.sink { fill-color: green; }\nnode.selected { fill-color: #13b8d1; }\nedge.short { fill-color: red; }");
		currentGraph = new GraphSummary(taintSummary);
		currentGraph.addMethodGraph(graph);
		SwingViewer viewer = new SwingViewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);

		viewer.enableAutoLayout(new SpringBox(false, new Random(42)));
		ViewerPipe viewerPipe = viewer.newViewerPipe();
		viewerPipe.addSink(graph);
		viewerPipe.addViewerListener(this);
		ViewPanel view = (ViewPanel) viewer.addDefaultView(false);
		view.enableMouseOptions();
		graphPanel = new JPanel(new BorderLayout());
		splitPane.setRightComponent(graphPanel);
		graphPanel.add(view);
		buttonGraph.setEnabled(false);
		repaint();

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (!Thread.interrupted()) {
					viewerPipe.pump();
				}
			}
		}).start();
	}

	@Override
	public void init(GraphicGraph graph, View view) {

	}

	@Override
	public void release() {

	}

	@Override
	public EnumSet<InteractiveElement> getManagedTypes() {
		return null;
	}


	@Override
	public void viewClosed(String viewName) {

	}

	@Override
	public void buttonPushed(String id) {

	}

	@Override
	public void buttonReleased(String id) {
		System.out.println(id);
		List<TaintMethodSegment> paths = taintSummary.methodMaps.get(id);
		if (paths != null && !paths.isEmpty()) {
			methodsList.setSelectedValue(paths.get(0), true);
		}
	}

	@Override
	public void mouseOver(String id) {
//		System.out.println(id);
	}

	@Override
	public void mouseLeft(String id) {
//		System.out.println(id);
	}

	private void onCancel() {
		// add your code here if necessary
		dispose();
	}

	public static void main(String[] args) {
		PathInfoVisualizeDialog dialog = new PathInfoVisualizeDialog(null, null);
		dialog.pack();
		dialog.setVisible(true);
		System.exit(0);
	}

	{
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
		$$$setupUI$$$();
	}

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		contentPane = new JPanel();
		contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
		contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
		final Spacer spacer1 = new Spacer();
		panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
		panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		buttonGraph = new JButton();
		buttonGraph.setText("Graph View");
		panel2.add(buttonGraph, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		checkAutoStatement = new JCheckBox();
		checkAutoStatement.setSelected(true);
		checkAutoStatement.setText("Auto To Statement");
		panel2.add(checkAutoStatement, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		checkCombineMethod = new JCheckBox();
		checkCombineMethod.setText("Combine Method");
		panel2.add(checkCombineMethod, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		checkAlwaysTop = new JCheckBox();
		checkAlwaysTop.setSelected(true);
		checkAlwaysTop.setText("Always On Top");
		panel2.add(checkAlwaysTop, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		splitPane.setOrientation(0);
		contentPane.add(splitPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
		final JPanel panel3 = new JPanel();
		panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		splitPane.setLeftComponent(panel3);
		final JScrollPane scrollPane1 = new JScrollPane();
		panel3.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		fieldsList = new JTabbedPane();
		scrollPane1.setViewportView(fieldsList);
		final JScrollPane scrollPane2 = new JScrollPane();
		fieldsList.addTab("Methods", scrollPane2);
		methodsList = new JList();
		scrollPane2.setViewportView(methodsList);
		final JScrollPane scrollPane3 = new JScrollPane();
		fieldsList.addTab("Statements", scrollPane3);
		statementsList = new JList();
		scrollPane3.setViewportView(statementsList);
		final JScrollPane scrollPane4 = new JScrollPane();
		fieldsList.addTab("Fields", scrollPane4);
		list3 = new JList();
		scrollPane4.setViewportView(list3);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return contentPane;
	}

}

package uk.co.darkerwaters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import uk.co.darkerwaters.DataGraph.DataGraphSeries;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.layout.RowData;

public class GraphsDialog extends Dialog {

	protected Object result;
	protected Shell shell;
	protected ArrayList<DataGraph> currentGraphs;
	private Combo comboActiveGraph;
	private Text txtTitle;
	private Text txtMaxy;
	private Text txtMiny;
	private Table tableSeries;
	private Button btnMinY;
	private Group grpSettings;
	private Spinner spinnerWidth;
	private Spinner spinnerHeight;
	private Button btnMaxY;
	private Button btnSeriesAdd;
	private Button btnSeriesDelete;
	private TableColumn tblclmnSeries;
	private TableColumn tblclmnColour;
	private TableColumn tblclmnLineWidth;
	private DataGraph selectedGraph;
	private TableItem tableItem;
	private TableItem tableItem_1;
	private TableItem tableItem_2;
	private TableItem tableItem_3;
	private TableItem tableItem_4;
	private Map<TableItem, TableEditor> editors = new HashMap<TableItem, TableEditor>();

	/**
	 * Create the dialog.
	 * @param parent
	 * @param currentGraphs 
	 * @param style
	 */
	public GraphsDialog(Shell parent, ArrayList<DataGraph> currentGraphs) {
		super(parent, SWT.DIALOG_TRIM | SWT.RESIZE);
		setText("Graphs");
		this.currentGraphs = currentGraphs;
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.setSize(450, 300);
		shell.setText(getText());
		shell.setLayout(new GridLayout(1, false));
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		composite.setBounds(0, 0, 64, 64);
		composite.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		Label lblGraph = new Label(composite, SWT.NONE);
		lblGraph.setText("Graph");
		
		this.comboActiveGraph = new Combo(composite, SWT.READ_ONLY);
		comboActiveGraph.setLayoutData(new RowData(100, SWT.DEFAULT));
		comboActiveGraph.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onActiveGraphSelectionChanged();
			}
		});
		comboActiveGraph.setItems(new String[] {"none selected"});
		comboActiveGraph.select(0);
		
		Button btnAddGraph = new Button(composite, SWT.NONE);
		btnAddGraph.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onAddGraph();
			}
		});
		btnAddGraph.setText("Add Graph");
		
		Button btnDeleteGraph = new Button(composite, SWT.NONE);
		btnDeleteGraph.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onDeleteGraph();
			}
		});
		btnDeleteGraph.setText("Delete Graph");
		
		grpSettings = new Group(composite, SWT.NONE);
		grpSettings.setText("Settings");
		grpSettings.setLayout(new GridLayout(2, true));
		
		Composite composite_1 = new Composite(grpSettings, SWT.NONE);
		composite_1.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		Label lblWidth = new Label(composite_1, SWT.NONE);
		lblWidth.setText("Width");
		
		spinnerWidth = new Spinner(composite_1, SWT.BORDER);
		spinnerWidth.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				onDataChanged(event);
			}
		});
		spinnerWidth.setMaximum(8);
		spinnerWidth.setMinimum(1);
		spinnerWidth.setSelection(8);
		
		Label label = new Label(composite_1, SWT.NONE);
		label.setText("/8");
		
		Composite composite_2 = new Composite(grpSettings, SWT.NONE);
		composite_2.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		Label lblHeight = new Label(composite_2, SWT.NONE);
		lblHeight.setText("Height");
		
		spinnerHeight = new Spinner(composite_2, SWT.BORDER);
		spinnerHeight.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				onDataChanged(event);
			}
		});
		spinnerHeight.setMaximum(8);
		spinnerHeight.setMinimum(1);
		
		Label label_1 = new Label(composite_2, SWT.NONE);
		label_1.setText("/8");
		
		Label lblTitle = new Label(grpSettings, SWT.NONE);
		lblTitle.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTitle.setText("Title");
		
		txtTitle = new Text(grpSettings, SWT.BORDER);
		GridData gd_txtTitle = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_txtTitle.minimumWidth = 100;
		txtTitle.setLayoutData(gd_txtTitle);
		txtTitle.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				onDataChanged(event);
			}
		});
		txtTitle.setText("Title");
		
		btnMaxY = new Button(grpSettings, SWT.CHECK);
		btnMaxY.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onSelectMaxY();
			}
		});
		btnMaxY.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnMaxY.setText("Max Y");
		
		txtMaxy = new Text(grpSettings, SWT.BORDER);
		GridData gd_txtMaxy = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_txtMaxy.minimumWidth = 50;
		txtMaxy.setLayoutData(gd_txtMaxy);
		txtMaxy.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				onDataChanged(event);
			}
		});
		txtMaxy.setText("MaxY");
		
		btnMinY = new Button(grpSettings, SWT.CHECK);
		btnMinY.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onSelectMinY();
			}
		});
		btnMinY.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnMinY.setText("Min Y");
		
		txtMiny = new Text(grpSettings, SWT.BORDER);
		GridData gd_txtMiny = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_txtMiny.minimumWidth = 50;
		txtMiny.setLayoutData(gd_txtMiny);
		txtMiny.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				onDataChanged(event);
			}
		});
		txtMiny.setText("MinY");
		
		tableSeries = new Table(grpSettings, SWT.BORDER | SWT.FULL_SELECTION);
		GridData gd_tableSeries = new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1);
		gd_tableSeries.heightHint = 100;
		gd_tableSeries.minimumHeight = 100;
		tableSeries.setLayoutData(gd_tableSeries);
		tableSeries.setHeaderVisible(true);
		tableSeries.setLinesVisible(true);
		
		tblclmnSeries = new TableColumn(tableSeries, SWT.NONE);
		tblclmnSeries.setResizable(false);
		tblclmnSeries.setWidth(100);
		tblclmnSeries.setText("Series");
		
		tblclmnColour = new TableColumn(tableSeries, SWT.NONE);
		tblclmnColour.setWidth(70);
		tblclmnColour.setText("Colour");
		
		tblclmnLineWidth = new TableColumn(tableSeries, SWT.NONE);
		tblclmnLineWidth.setWidth(70);
		tblclmnLineWidth.setText("Line Width");
		
		tableItem = new TableItem(tableSeries, SWT.NONE);
		tableItem.setText("New TableItem");
		
		tableItem_1 = new TableItem(tableSeries, SWT.NONE);
		tableItem_1.setText("New TableItem");
		
		tableItem_2 = new TableItem(tableSeries, SWT.NONE);
		tableItem_2.setText("New TableItem");
		
		tableItem_3 = new TableItem(tableSeries, SWT.NONE);
		tableItem_3.setText("New TableItem");
		
		tableItem_4 = new TableItem(tableSeries, SWT.NONE);
		tableItem_4.setText("New TableItem");
		
		btnSeriesAdd = new Button(grpSettings, SWT.NONE);
		btnSeriesAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onClickSeriesAdd();
			}
		});
		btnSeriesAdd.setText("Add");
		
		btnSeriesDelete = new Button(grpSettings, SWT.NONE);
		btnSeriesDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onClickSeriesDelete();
			}
		});
		btnSeriesDelete.setText("Delete");
		
		populateGraphCombo();

	}

	protected void onDataChanged(ModifyEvent event) {
		// update the data on the selected graph
		if (null != this.selectedGraph) {
			this.selectedGraph.title = txtTitle.getText();
			this.selectedGraph.widthX = spinnerWidth.getSelection();
			this.selectedGraph.widthY = spinnerHeight.getSelection();
			onSelectMaxY();
			onSelectMinY();
		}
	}

	protected void onClickSeriesAdd() {
		// add a new series
		if (null != this.selectedGraph) {
			this.selectedGraph.addDataSeries(DataGraph.getAvailableSeriesHeading(0), 0, 100);
			this.showGraphData(this.selectedGraph);
		}
	}

	protected void onClickSeriesDelete() {
		// get the selected table item and delete it
		for (TableItem item : tableSeries.getSelection()) {
			// remove the series from the graph
			if (null != this.selectedGraph) {
				DataGraphSeries selectedSeries = (DataGraphSeries)item.getData();
				selectedGraph.graphSeries.remove(selectedSeries);
			}
		}
		// repopulate our data
		showGraphData(this.selectedGraph);
	}

	protected void onSelectMinY() {
		txtMiny.setEnabled(btnMinY.getSelection());
		if (null != this.selectedGraph) {
			if (this.btnMinY.getSelection()) {
				// set the min
				try {
					this.selectedGraph.min = Double.parseDouble(txtMiny.getText());
				}
				catch (Exception e) {
					// fine
					this.selectedGraph.min = null;
				}
			}
			else {
				// no min
				this.selectedGraph.min = null;
			}
		}
	}

	protected void onSelectMaxY() {
		txtMaxy.setEnabled(btnMaxY.getSelection());
		if (null != this.selectedGraph) {
			if (this.btnMaxY.getSelection()) {
				// set the max
				try {
					this.selectedGraph.max = Double.parseDouble(txtMaxy.getText());
				}
				catch (Exception e) {
					// fine
					this.selectedGraph.max = null;
				}
			}
			else {
				// no max
				this.selectedGraph.max = null;
			}
		}
	}

	protected void onActiveGraphSelectionChanged() {
		// find the correct graph and show it's data in the controls
		String selectedTitle = this.comboActiveGraph.getText();
		this.selectedGraph = null;
		for (DataGraph graph : this.currentGraphs) {
			if (graph.getId().equals(selectedTitle)) {
				// select this data
				selectedGraph = graph;
				break;
			}
		}
		showGraphData(selectedGraph);
	}

	private void showGraphData(DataGraph graph) {
		this.selectedGraph = null;
		
		boolean isEnabled = null != graph;
		spinnerWidth.setEnabled(isEnabled);
		spinnerHeight.setEnabled(isEnabled);
		txtTitle.setEnabled(isEnabled);
		btnMaxY.setEnabled(isEnabled);
		txtMaxy.setEnabled(isEnabled);
		btnMinY.setEnabled(isEnabled);
		txtMiny.setEnabled(isEnabled);
		tableSeries.setEnabled(isEnabled);
		btnSeriesAdd.setEnabled(isEnabled);
		btnSeriesDelete.setEnabled(isEnabled);

		// clear the table
		while (tableSeries.getItemCount() > 0) {
			TableItem item = tableSeries.getItem(0);
			TableEditor tableEditor = this.editors.remove(item);
			if (null != tableEditor) {
				tableEditor.getEditor().dispose();
				tableEditor.dispose();
			}
			item.dispose();
		}
		
		if (null == graph) {
			// clear everything
			txtTitle.setText("");
			btnMaxY.setSelection(false);
			txtMaxy.setText("null");
			btnMinY.setSelection(false);
			txtMiny.setText("null");
		}
		else {
			// show all the data
			txtTitle.setText(graph.getTitle() == null ? graph.getId() : graph.getTitle());
			spinnerHeight.setSelection(graph.getWidthY());
			spinnerWidth.setSelection(graph.getWidthX());
			
			Double minY = graph.getMin();
			btnMinY.setSelection(minY != null);
			txtMiny.setText(Double.toString(minY != null ? minY : 0));
			txtMiny.setEnabled(minY != null);
			
			Double maxY = graph.getMax();
			btnMaxY.setSelection(maxY != null);
			txtMaxy.setText(Double.toString(maxY != null ? maxY : 0));
			txtMaxy.setEnabled(maxY != null);
			
			for (int i = 0; i < graph.getNumberDataSeries(); ++i) {
				DataGraphSeries series = graph.getDataSeries(i);
				TableItem item = new TableItem(tableSeries, SWT.NONE);
				item.setText(0, series.seriesTitle);
				item.setText(1, series.colour == null ? "null" : series.colour.toString());
				item.setText(2, Integer.toString(series.lineWidth));
				item.setData(series);
				
				TableEditor editor = new TableEditor (tableSeries);
				editor.grabHorizontal = true;
				editor.setEditor(createSeriesCombo(series, item), item, 0);
				this.editors.put(item,  editor);
				
			}
		}
		this.selectedGraph = graph;
	}

	private Control createSeriesCombo(final DataGraphSeries series, final TableItem item) {
		Combo combo = new Combo (tableSeries, SWT.READ_ONLY);
		int selection = -1;
		for (int i = 0; i < DataGraph.getAvailableSeries(); ++i) {
			combo.add(DataGraph.getAvailableSeriesHeading(i));
			if (series.seriesIndex == i) {
				selection = i;
			}
		}
		combo.select(selection);
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tableSeries.setSelection(item);
				series.seriesIndex = combo.getSelectionIndex(); 
				series.seriesTitle = combo.getText();
				item.setText(0, series.seriesTitle);
			}
		});
		return combo;
	}

	private void onAddGraph() {
		DataGraph graph = new DataLineGraph("Graph " + (this.currentGraphs.size() + 1));
		this.currentGraphs.add(graph);
		// add to the combo
		comboActiveGraph.add(graph.getId());
		// select this
		comboActiveGraph.select(comboActiveGraph.getItemCount() - 1);
		this.selectedGraph = graph;
		showGraphData(graph);
	}

	private void onDeleteGraph() {
		if (null != selectedGraph) {
			this.currentGraphs.remove(this.selectedGraph);
			populateGraphCombo();
		}
	}

	private void populateGraphCombo() {
		comboActiveGraph.removeAll();
		for (DataGraph graph : this.currentGraphs) {
			comboActiveGraph.add(graph.getId());
		}
		// remove from the combo
		comboActiveGraph.select(comboActiveGraph.getItemCount() - 1);
		onActiveGraphSelectionChanged();
	}

	public void dispose() {
		shell.dispose();
	}
}

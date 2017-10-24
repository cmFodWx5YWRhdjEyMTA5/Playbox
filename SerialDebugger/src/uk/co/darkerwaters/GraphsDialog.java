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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import uk.co.darkerwaters.DataGraph.DataGraphSeries;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.events.ModifyListener;
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
	private TableColumn tblclmnSamples;
	private Group groupSeries;
	private Combo comboSeriesSeries;
	private Combo comboSeriesColour;
	private Combo comboSeriesLineWidth;
	private Spinner spinnerSeriesSamples;
	private DataGraphSeries selectedGraphSeries;

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
		shell.setSize(430, 500);
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
		
		tableSeries = new Table(grpSettings, SWT.BORDER);
		tableSeries.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onTableSeriesSelectionChanged();
			}
		});
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
		
		tblclmnSamples = new TableColumn(tableSeries, SWT.NONE);
		tblclmnSamples.setWidth(50);
		tblclmnSamples.setText("Samples");
		
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
		
		groupSeries = new Group(composite, SWT.NONE);
		groupSeries.setLayout(new GridLayout(4, false));
		
		Label lblSeries = new Label(groupSeries, SWT.NONE);
		lblSeries.setText("Series");
		
		Label lblColour = new Label(groupSeries, SWT.NONE);
		lblColour.setText("Colour");
		
		Label lblLineWidth = new Label(groupSeries, SWT.NONE);
		lblLineWidth.setText("Line Width");
		
		Label lblSamples = new Label(groupSeries, SWT.NONE);
		lblSamples.setText("Samples");
		
		comboSeriesSeries = new Combo(groupSeries, SWT.READ_ONLY);
		GridData gd_comboSeriesSeries = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_comboSeriesSeries.minimumWidth = 100;
		comboSeriesSeries.setLayoutData(gd_comboSeriesSeries);
		comboSeriesSeries.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				onSeriesDataChanged();
			}
		});
		
		comboSeriesColour = new Combo(groupSeries, SWT.READ_ONLY);
		comboSeriesColour.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		comboSeriesColour.setItems(new String[] {"red", "blue", "green", "yellow", "white"});
		comboSeriesColour.select(2);
		comboSeriesColour.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				onSeriesDataChanged();
			}
		});
		
		comboSeriesLineWidth = new Combo(groupSeries, SWT.READ_ONLY);
		comboSeriesLineWidth.setItems(new String[] {"thin", "thick", "thicker", "thickest"});
		comboSeriesLineWidth.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		comboSeriesLineWidth.select(2);
		comboSeriesLineWidth.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				onSeriesDataChanged();
			}
		});
		
		spinnerSeriesSamples = new Spinner(groupSeries, SWT.BORDER | SWT.READ_ONLY);
		spinnerSeriesSamples.setIncrement(50);
		spinnerSeriesSamples.setPageIncrement(100);
		spinnerSeriesSamples.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				onSeriesDataChanged();
			}
		});
		spinnerSeriesSamples.setMaximum(1000);
		spinnerSeriesSamples.setMinimum(100);
		
		btnSeriesAdd = new Button(groupSeries, SWT.NONE);
		btnSeriesAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onClickSeriesAdd();
			}
		});
		btnSeriesAdd.setText("Add");
		
		btnSeriesDelete = new Button(groupSeries, SWT.NONE);
		btnSeriesDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onClickSeriesDelete();
			}
		});
		btnSeriesDelete.setText("Delete");
		new Label(groupSeries, SWT.NONE);
		new Label(groupSeries, SWT.NONE);
		
		populateGraphCombo();

	}
	
	public static Color IndexToColour(int index) {
		Color color;
		switch(index) {
		case 0 :
			color = DataGraph.K_RED;
			break;
		case 1 :
			color = DataGraph.K_BLUE;
			break;
		case 2 :
		default:
			color = DataGraph.K_GREEN;
			break;
		case 3 :
			color = DataGraph.K_YELLOW;
			break;
		case 4 :
			color = DataGraph.K_WHITE;
			break;
		}
		return color;
	}
	
	public static int ColourToIndex (Color colour) {
		int selection = 2;
		if (colour == DataGraph.K_RED) {
			selection = 0;
		}
		else if (colour == DataGraph.K_BLUE) {
			selection = 1;
		}
		else if (colour == DataGraph.K_GREEN) {
			selection = 2;
		}
		else if (colour == DataGraph.K_YELLOW) {
			selection = 3;
		}
		else if (colour == DataGraph.K_WHITE) {
			selection = 4;
		}
		return selection;
	}

	protected void onSeriesDataChanged() {
		if (this.selectedGraphSeries != null) {
			// update the series data from the controls
			this.selectedGraphSeries.seriesIndex = this.comboSeriesSeries.getSelectionIndex();
			this.selectedGraphSeries.seriesTitle = this.comboSeriesSeries.getText();
			// line colour
			this.selectedGraphSeries.colour = IndexToColour(this.comboSeriesColour.getSelectionIndex());
			// line width
			this.selectedGraphSeries.lineWidth = this.comboSeriesLineWidth.getSelectionIndex() + 1;
			// and the samples
			this.selectedGraphSeries.seriesCount = this.spinnerSeriesSamples.getSelection();
			
			// show this new data in the table too
			showGraphData(this.selectedGraph);
		}
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
			this.selectedGraph.addDataSeries(DataGraph.getAvailableSeriesHeading(0), 0);
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
		this.selectedGraphSeries = null;
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

	private void onTableSeriesSelectionChanged() {
		// find the correctly selected series for the selected graph
		if (tableSeries.getSelectionCount() == 1 && this.selectedGraph != null) {
			// this is a good selection
			TableItem selectedTableItem = tableSeries.getSelection()[0];
			this.selectedGraphSeries = (DataGraphSeries) selectedTableItem.getData();
		}
		else {
			// this is a bad selection
			this.selectedGraphSeries = null;
		}
		// show this data now it is selected
		showGraphSeriesData(this.selectedGraphSeries);
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
		int selectedSeries = tableSeries.getSelectionIndex();
		while (tableSeries.getItemCount() > 0) {
			tableSeries.getItem(0).dispose();
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
				item.setText(3, Integer.toString(series.seriesCount));
				item.setData(series);
			}
			tableSeries.setSelection(selectedSeries);
			if (tableSeries.getSelectionCount() == 1) {
				this.selectedGraphSeries = (DataGraphSeries) tableSeries.getSelection()[0].getData();
			}
		}
		this.selectedGraph = graph;
		
		showGraphSeriesData(this.selectedGraphSeries);
	}
	
	private void showGraphSeriesData(DataGraphSeries series) {
		this.selectedGraphSeries = null;
		
		this.comboSeriesSeries.setEnabled(series != null);
		this.comboSeriesColour.setEnabled(series != null);
		this.comboSeriesLineWidth.setEnabled(series != null);
		this.spinnerSeriesSamples.setEnabled(series != null);
		
		if (series != null) {
			// show the correct data
			this.comboSeriesSeries.removeAll();
			int selection = -1;
			for (int i = 0; i < DataGraph.getAvailableSeries(); ++i) {
				this.comboSeriesSeries.add(DataGraph.getAvailableSeriesHeading(i));
				if (series.seriesIndex == i) {
					selection = i;
				}
			}
			this.comboSeriesSeries.select(selection);
			
			// get the correct colour
			this.comboSeriesColour.select(ColourToIndex(series.colour));
			
			// and the line width
			this.comboSeriesLineWidth.select(series.lineWidth - 1);
			
			// and the samples
			this.spinnerSeriesSamples.setSelection(series.seriesCount);
		}
		this.selectedGraphSeries = series;
	}

	private void onAddGraph() {
		DataGraph graph = new DataLineGraph("Graph " + (this.currentGraphs.size() + 1));
		this.currentGraphs.add(graph);
		// add to the combo
		comboActiveGraph.add(graph.getId());
		// select this
		comboActiveGraph.select(comboActiveGraph.getItemCount() - 1);
		this.selectedGraph = graph;
		this.selectedGraphSeries = null;
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
		this.selectedGraphSeries = null;
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

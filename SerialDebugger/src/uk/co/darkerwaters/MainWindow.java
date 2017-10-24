package uk.co.darkerwaters;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import java.util.ArrayList;
import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.fazecast.jSerialComm.SerialPort;

import uk.co.darkerwaters.CommsPortManager.CommsPortManagerListener;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridLayout;
import swing2swt.layout.BorderLayout;
import org.eclipse.swt.layout.GridData;

public class MainWindow {

	protected Shell shell;

	private Table tblDataTable;
	private Text txtConsoletext;
	private Combo comboPort;
	private Button btnRefresh;
	private Combo comboBaud;
	private Combo comboDataBits;
	private Combo comboStopBits;

	private CommsPortManager portManager;

	private Combo comboParity;

	private Button btnConnect;
	
	private final ArrayList<String> stringsReceived = new ArrayList<String>();
	boolean isStringExecRequired = true;

	private boolean isConsolePaused = false;

	private Button btnConsolePause;
	private Table tableCurrentData;
	private Text txtComments;

	private Canvas graphCanvas;
	
	private final ArrayList<DataGraph> currentGraphs = new ArrayList<DataGraph>();

	private GraphsDialog dialog = null;

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 * @wbp.parser.entryPoint
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(1020, 800);
		shell.setText("Serial Debugger");
		shell.setLayout(new FillLayout(SWT.VERTICAL));
		
		this.portManager = CommsPortManager.getManager();
		CommsPortManagerListener managerListener = new CommsPortManagerListener() {
			@Override
			public void onCommsPortDataReceived(final String string) {
				MainWindow.this.onCommsPortDataReceived(string);
			}
		};
		this.portManager.addListener(managerListener);
		shell.addDisposeListener(new DisposeListener() {	
			@Override
			public void widgetDisposed(DisposeEvent event) {
				// disconnect when this window is closed
				MainWindow.this.portManager.removeListener(managerListener);
				MainWindow.this.portManager.disconnect();
			}
		});
		
		SashForm sashForm = new SashForm(shell, SWT.VERTICAL);
		
		Composite compositeTop = new Composite(sashForm, SWT.NONE);
		compositeTop.setLayout(new GridLayout(1, false));
		
		Composite topCompositeCommsRow = new Composite(compositeTop, SWT.NONE);
		topCompositeCommsRow.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		this.btnConnect = new Button(topCompositeCommsRow, SWT.NONE);
		btnConnect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MainWindow.this.onConnectPort();
			}
		});
		btnConnect.setText("Connect");
		
		Label lblPort = new Label(topCompositeCommsRow, SWT.NONE);
		lblPort.setText("Port");
		
		this.comboPort = new Combo(topCompositeCommsRow, SWT.READ_ONLY);
		
		this.btnRefresh = new Button(topCompositeCommsRow, SWT.NONE);
		btnRefresh.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MainWindow.this.fillComboPort();
				// set the controls from this selected port
				setPortControls();
			}
		});
		btnRefresh.setText("Refresh");
		
		Label lblBaud = new Label(topCompositeCommsRow, SWT.NONE);
		lblBaud.setText("Baud");
		
		this.comboBaud = new Combo(topCompositeCommsRow, SWT.READ_ONLY);
		comboBaud.setItems(new String[] {"300", "1200", "2400", "4800", "9600", "14400", "19200", "28800", "38400", "57600", "115200", "230400"});
		comboBaud.select(4);
		
		Label lblDataBits = new Label(topCompositeCommsRow, SWT.NONE);
		lblDataBits.setText("Data Bits");
		
		this.comboDataBits = new Combo(topCompositeCommsRow, SWT.READ_ONLY);
		comboDataBits.setItems(new String[] {"5", "6", "7", "8", "9"});
		comboDataBits.select(3);
		
		Label lblStopbits = new Label(topCompositeCommsRow, SWT.NONE);
		lblStopbits.setText("Stop Bits");
		
		this.comboStopBits = new Combo(topCompositeCommsRow, SWT.READ_ONLY);
		comboStopBits.setItems(new String[] {"1", "1.5", "2"});
		comboStopBits.select(0);
		
		Label lblParity = new Label(topCompositeCommsRow, SWT.NONE);
		lblParity.setText("Parity");
		
		this.comboParity = new Combo(topCompositeCommsRow, SWT.READ_ONLY);
		comboParity.setItems(new String[] {"none", "odd", "even", "mark", "space"});
		comboParity.select(0);
		
		Composite topCompositeDataRow = new Composite(compositeTop, SWT.NONE);
		topCompositeDataRow.setLayout(new FillLayout(SWT.HORIZONTAL));
		topCompositeDataRow.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		
		tableCurrentData = new Table(topCompositeDataRow, SWT.BORDER);
		tableCurrentData.setHeaderVisible(true);
		tableCurrentData.setLinesVisible(true);
		comboPort.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MainWindow.this.onCommsPortSelectionChanged();
			}
		});
		
		Composite topCompositeCommentsRow = new Composite(compositeTop, SWT.NONE);
		topCompositeCommentsRow.setLayout(new FillLayout(SWT.HORIZONTAL | SWT.VERTICAL));
		topCompositeCommentsRow.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		txtComments = new Text(topCompositeCommentsRow, SWT.BORDER);
		txtComments.setText("");
		
		Composite compositeBottom = new Composite(sashForm, SWT.BORDER);
		compositeBottom.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Composite compositeTabArea = new Composite(compositeBottom, SWT.NONE);
		compositeTabArea.setLayout(new BorderLayout(0, 0));
		
		TabFolder tabFolder = new TabFolder(compositeTabArea, SWT.NONE);
		
		TabItem tbtmTextConsole = new TabItem(tabFolder, SWT.NONE);
		tbtmTextConsole.setText("Text Console");
		
		Composite textConsoleComposite = new Composite(tabFolder, SWT.NONE);
		tbtmTextConsole.setControl(textConsoleComposite);
		textConsoleComposite.setLayout(new BorderLayout(0, 0));
		
		txtConsoletext = new Text(textConsoleComposite, SWT.BORDER);
		txtConsoletext.setText("consoleText");
		
		TabItem tbtmDataTable = new TabItem(tabFolder, SWT.NONE);
		tbtmDataTable.setText("Data Table");
		
		tblDataTable = new Table(tabFolder, SWT.BORDER | SWT.FULL_SELECTION);
		tbtmDataTable.setControl(tblDataTable);
		tblDataTable.setHeaderVisible(true);
		tblDataTable.setLinesVisible(true);
		
		TabItem tbtmDataGraph = new TabItem(tabFolder, SWT.NONE);
		tbtmDataGraph.setText("Data Graph");
		
		Composite graphComposite = new Composite(tabFolder, SWT.NONE);
		tbtmDataGraph.setControl(graphComposite);
		graphComposite.setLayout(new BorderLayout(0, 0));
		
		this.graphCanvas = new Canvas(graphComposite, SWT.NO_REDRAW_RESIZE);
		final Display display = shell.getDisplay();
		this.graphCanvas.addPaintListener(new PaintListener() { 
	        public void paintControl(PaintEvent e) { 
				// and update the graphs
	        		MainWindow.this.updateGraphDisplays(e, display);
	        } 
	    });
		
		Composite textConsoleButtonsComposite = new Composite(compositeTabArea, SWT.NONE);
		textConsoleButtonsComposite.setLayoutData(BorderLayout.SOUTH);
		textConsoleButtonsComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		this.btnConsolePause = new Button(textConsoleButtonsComposite, SWT.NONE);
		btnConsolePause.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MainWindow.this.onConsolePause();
			}
		});
		btnConsolePause.setText("Pause");
		
		Button btnConsoleClear = new Button(textConsoleButtonsComposite, SWT.NONE);
		btnConsoleClear.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MainWindow.this.onConsoleClear();
			}
		});
		btnConsoleClear.setText("Clear");
		
		Button btnGraphs = new Button(textConsoleButtonsComposite, SWT.NONE);
		btnGraphs.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MainWindow.this.onGraphsButton();
			}
		});
		btnGraphs.setText("Graphs");
		
		sashForm.setWeights(new int[] {1, 3});
		fillComboPort();
		
		// set the controls from the current selected port
		setPortControls();
		
		// fill the table
		fillCurrentTableData("");
	}

	protected void updateGraphDisplays(PaintEvent e, Display display) {
		Rectangle rectangle = graphCanvas.getClientArea();
		// fill the graph area a nice black...
		GC gc = e.gc;
		gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK)); 
        gc.fillRectangle(rectangle);
        Rectangle canvasRect = new Rectangle(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        // and all the graphs now
        int cellHeight = rectangle.height / 8;
        int cellWidth = rectangle.width / 8;
		if (currentGraphs.size() > 0) {
			for (DataGraph graph : this.currentGraphs) {
				// get the rect for this graph to draw in
				rectangle.width = graph.widthX * cellWidth;
				rectangle.height = graph.widthY * cellHeight;
				graph.drawDataSeries(new Rectangle(rectangle.x, rectangle.y, rectangle.width - 5, rectangle.height - 5), gc, display);
				// if this rect is at the edge of our rect then move the next one below it
				if (rectangle.width + rectangle.x > canvasRect.width - (cellWidth / 2)) {
					rectangle.y += rectangle.height;
					rectangle.x = canvasRect.x;
				}
				else {
					// move it across
					rectangle.x += rectangle.width;
				}
			}
		}
	}

	protected void onConsoleClear() {
		// clear the contents of the console
		this.txtConsoletext.setText("");
		this.tblDataTable.removeAll();
	}

	protected void onConsolePause() {
		// pause writing the console data
		if (this.isConsolePaused) {
			// resume
			this.btnConsolePause.setText("Pause");
			this.isConsolePaused = false;
		}
		else {
			// pause
			this.btnConsolePause.setText("Resume");
			this.isConsolePaused = true;
		}
	}

	protected void onCommsPortDataReceived(String string) {
		// update the data on the console, this comes on a new thread so we want to do this
		// as quickly as possible, but one runnable per thing is bad, collect the data here
		synchronized (this.stringsReceived) {
			this.stringsReceived.add(string);
			if (this.isStringExecRequired) {
				// we need to create a new runnable to deal with this data
				Display.getDefault().asyncExec(new Runnable() {
				    public void run() {
				    		MainWindow.this.updateDataReceived();
				    }
				});
				// don't make another, this will show all the strings in the list when it eventually runs
				this.isStringExecRequired = false;
			}
		}
	}

	protected void updateDataReceived() {
		// clear when it gets too much
		if (this.txtConsoletext.isDisposed()) {
			// control dead, quit
			return;
		}
		if (this.txtConsoletext.getCharCount() > 60000) {
			// clear the first line out, too long
			this.txtConsoletext.setText("");
		}
		String[] strings;
		synchronized (this.stringsReceived) {
			if (!this.isConsolePaused) {
				// only get the data if we aren't paused
				strings = this.stringsReceived.toArray(new String[this.stringsReceived.size()]);
				this.stringsReceived.clear();
			}
			else {
				// no strings
				strings = new String[0];
			}
			// we have all the strings now, we will need a new exec command for any more
			this.isStringExecRequired = true;
		}
		// append the text to the console
		for (String string : strings) {
			// and append the new string
	        this.txtConsoletext.append(string + Text.DELIMITER);
	        if (string.startsWith("{D}")) {
	        		// this is data, put in the data table
	        		TableItem item = new TableItem(this.tblDataTable, SWT.NONE, 0);
				String[] dataEntries = string.substring(3).split("\\|");
				if (dataEntries.length > 0) {
					// ensure we have enough columns in the list...
					int colWidth = this.tblDataTable.getClientArea().width / dataEntries.length;
					while (this.tblDataTable.getColumnCount() < dataEntries.length) {
						setTableColumn(this.tblDataTable, this.tblDataTable.getColumnCount(), "Column", colWidth);
					}
				}
				for (int i = 0; i < dataEntries.length; ++i) {
					// set the text on the one item in the table
					item.setText(i, dataEntries[i]);
				}
				// and keep to a sensible number
				while (this.tblDataTable.getItemCount() > 50) {
					this.tblDataTable.getItems()[this.tblDataTable.getItemCount() - 1].dispose();
				}
				// also add to the graph
				DataGraph.addData(dataEntries);
				this.graphCanvas.redraw();
	        }
	        else if (string.startsWith("{H}")) {
	        		// this is headings, update the graphs with this
		        	for (DataGraph graph : this.currentGraphs) {
		        		graph.updateGraphHeadings(string.substring(3).split("\\|"));
				}
	        }
	        else {
	        		// show this as a comment
	        		this.txtComments.append(string + Text.DELIMITER);
	        }
		}
		if (strings.length > 0) {
			// and show the latest data
			fillCurrentTableData(strings[strings.length - 1]);
		}
	}

	protected void onConnectPort() {
		// connect to the selected port
		if (this.portManager.isConnected()) {
			// disconnect this port
			if (this.portManager.disconnect()) {
				// disconnected
				
			}
		}
		else {
			// connect this port
			if (this.portManager.connect()) {
				// connected
			
			}
		}
		if (this.portManager.isConnected()) {
			this.btnConnect.setText("Disconnect");
			this.comboPort.setEnabled(false);
		}
		else {
			this.btnConnect.setText("Connect");
			this.comboPort.setEnabled(true);
		}
		setPortControls();
	}

	protected void onCommsPortSelectionChanged() {
		int index = this.comboPort.getSelectionIndex();
		if (index > -1) {
			String item = this.comboPort.getItem(index);
			// find this item in the list of combo ports
			this.portManager.setSelectedPort(item);
		}
		else {
			this.portManager.setSelectedPort(null);
		}
		// set the controls from this selected port
		setPortControls();
	}

	private void setPortControls() {
		// set the data from the selected port
		SerialPort port = this.portManager.getSelectedPort();
		boolean enableControls = null != port && !this.portManager.isConnected();
			
		this.btnConnect.setEnabled(null != port);
		this.btnRefresh.setEnabled(enableControls);
		this.comboBaud.setEnabled(enableControls);
		this.comboDataBits.setEnabled(enableControls);
		this.comboStopBits.setEnabled(enableControls);
		this.comboParity.setEnabled(enableControls);
		
		if (null != port) {
			// set the baud rate to that on the port
			String baudRate = Integer.toString(port.getBaudRate());
			for (int i = 0; i < this.comboBaud.getItemCount(); ++i) {
				if (this.comboBaud.getItem(i).equals(baudRate)) {
					// this is the baud rate
					this.comboBaud.select(i);
					break;
				}
			}
			// set the data bits
			String dataBits = Integer.toString(port.getNumDataBits());
			for (int i = 0; i < this.comboDataBits.getItemCount(); ++i) {
				if (this.comboDataBits.getItem(i).equals(dataBits)) {
					// this is the number of data bits
					this.comboDataBits.select(i);
					break;
				}
			}
			// set the stop bits
			String stopBits = CommsPortManager.StopBitsToString(port.getNumStopBits());
			for (int i = 0; i < this.comboStopBits.getItemCount(); ++i) {
				if (this.comboStopBits.getItem(i).equals(stopBits)) {
					// this is the number of stop bits
					this.comboStopBits.select(i);
					break;
				}
			}
			// set the parity
			String parity = CommsPortManager.ParityToString(port.getParity());
			for (int i = 0; i < this.comboParity.getItemCount(); ++i) {
				if (this.comboParity.getItem(i).equals(parity)) {
					// this is the parity
					this.comboParity.select(i);
					break;
				}
			}
		}
	}

	private void fillComboPort() {
		this.comboPort.removeAll();
		// get all the ports currently available
		for (String portName : this.portManager.listPorts()) {
			this.comboPort.add(portName);
		}
		this.portManager.setSelectedPort(null);
	}

	private void fillCurrentTableData(String dataLine) {
		if (dataLine.startsWith("{H}")) {
			// this is heading data, check it is the same or replace...
			String[] headings = dataLine.substring(3).split("\\|");
			if (headings.length > 0) {
				// do the headings as there are some to do
				int colWidth = this.tableCurrentData.getClientArea().width / headings.length;
				for (int i = 0; i < headings.length; ++i) {
					// for each heading, ensure there is a column that matches
					setTableColumn(this.tableCurrentData, i, headings[i], colWidth);
					setTableColumn(this.tblDataTable, i, headings[i], colWidth);
				}
			}
			// delete any left over from other headings
			removeUnwantedTableColumns(this.tableCurrentData, headings.length);
			removeUnwantedTableColumns(this.tblDataTable, headings.length);
		}
		else if (dataLine.startsWith("{D}")) {
			// this is data, show this data
			TableItem item;
			if (this.tableCurrentData.getItemCount() == 0) {
				// there is no data item
				item = new TableItem(tableCurrentData, SWT.NONE);
			}
			else {
				item = this.tableCurrentData.getItem(0);
			}
			String[] strings = dataLine.substring(3).split("\\|");
			// ensure we have enough columns in the list...
			int colWidth = this.tableCurrentData.getClientArea().width / strings.length;
			while (this.tableCurrentData.getColumnCount() < strings.length) {
				setTableColumn(this.tableCurrentData, this.tableCurrentData.getColumnCount(), "Column", colWidth);
			}
			for (int i = 0; i < strings.length; ++i) {
				// set the text on the one item in the table
				item.setText(i, strings[i]);
			}
		}
	}

	private void removeUnwantedTableColumns(Table table, int length) {
		while (table.getColumnCount() > length) {
			// while there are too many columns, delete the ones at the end
			table.getColumns()[table.getColumnCount() - 1].dispose();
		}
	}

	private void setTableColumn(Table table, final int index, String title, int width) {
		TableColumn column;
		if (table.getColumnCount() > index) {
			// just get the existing column
			column = table.getColumn(index);
		}
		else {
			// make a new column
			column = new TableColumn(table, SWT.CENTER);
		}
		// set the title
		column.setText(title);
		// and the width
		column.setWidth(width);
	}

	protected void onGraphsButton() {
		// TODO Auto-generated method stub
		if (dialog != null) {
			dialog.dispose();
		}
		dialog = new GraphsDialog(shell, this.currentGraphs);
		dialog.open();
	}
}

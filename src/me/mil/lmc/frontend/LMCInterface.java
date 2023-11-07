package me.mil.lmc.frontend;

import me.mil.lmc.LMCReader;
import me.mil.lmc.LMCWriter;
import me.mil.lmc.backend.AbstractObservableProcessor;

import me.mil.lmc.backend.exceptions.LMCException;
import me.mil.lmc.backend.exceptions.LMCRuntimeException;
import me.mil.lmc.frontend.swing.components.*;
import me.mil.lmc.frontend.util.DialogMessageType;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

// (not an interface) // todo convert to abstract class/interface for less unnecessary functions/fields visible here
public class LMCInterface {

	private final JFrame frame;

	// Panels
	private final RootPanel rootPanel;

	private final InputOutputPanel inputOutputPanel;
	private final InputPanel inputPanel;
	private final OutputPanel outputPanel;

	private final ControlPanel controlPanel;
	private final RegisterViewPanel registerViewPanel;
	private final MemoryViewPanel memoryViewPanel;
	///

	private LMCReader reader;
	private LMCWriter writer;

	private AbstractObservableProcessor processor;
	private final List<LMCProcessorObserver> processorObservers = new ArrayList<>();

	public LMCInterface() {
		this.frame = generateFrame();

		this.reader = new LMCGraphicalReader(this);
		this.writer = new LMCGraphicalWriter(this);

		// Panels
		this.rootPanel = new RootPanel(this);

		this.inputOutputPanel = new InputOutputPanel(this);
		this.inputOutputPanel.setInputPanel(new InputPanel(this));
		this.inputOutputPanel.setOutputPanel(new OutputPanel(this));
		this.inputPanel = inputOutputPanel.getInputPanel();
		this.outputPanel = inputOutputPanel.getOutputPanel();

		this.controlPanel = new ControlPanel(this);
		this.registerViewPanel = new RegisterViewPanel(this);
		this.memoryViewPanel = new MemoryViewPanel(this);
		//
		frame.setVisible(true);

	}

	private JFrame generateFrame() {
		JFrame frame = new JFrame();
		frame.setTitle("LMC");
		frame.setSize(1400, 800);
		frame.setLocationRelativeTo(null); // Centre
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		frame.setJMenuBar(LMCMenuBar.generate(this));
		return frame;
	}

	private void updateProcessorObservers() {
		processorObservers.forEach(po -> po.setProcessor(processor));
	}

	protected void addProcessorObserver(LMCProcessorObserver processorObserver) {
		processorObservers.add(processorObserver);
	}

	public void performControlFunction(ControlFunction function) {
		function.executeAction(this);
	}

	public void showMessageDialog(String title, String description, DialogMessageType messageType) {
		JOptionPane.showMessageDialog(getFrame(), description, title, messageType.getValue());
	}

	protected String showInputDialog(String description) {
		return JOptionPane.showInputDialog(getFrame(), description);
	}

	protected void showErrorDialog(LMCException error) {
		showMessageDialog((error instanceof LMCRuntimeException) ? "Runtime Error" : "Compilation Error",
				error.getMessage(), DialogMessageType.ERROR_MESSAGE);
	}

	// -- Getters / Setters -- //

	public JFrame getFrame() {
		return frame;
	}

	public RootPanel getRootPanel() {
		return rootPanel;
	}

	public InputOutputPanel getInputOutputPanel() {
		return inputOutputPanel;
	}

	public InputPanel getInputPanel() {
		return inputPanel;
	}

	public OutputPanel getOutputPanel() {
		return outputPanel;
	}

	public ControlPanel getControlPanel() {
		return controlPanel;
	}

	public RegisterViewPanel getRegisterViewPanel() {
		return registerViewPanel;
	}

	public MemoryViewPanel getMemoryViewPanel() {
		return memoryViewPanel;
	}

	public LMCReader getReader() {
		return reader;
	}

	public LMCWriter getWriter() {
		return writer;
	}

	public AbstractObservableProcessor getProcessor() {
		return processor;
	}

	protected void setProcessor(AbstractObservableProcessor newProcessor) {
		this.processor = newProcessor;
		updateProcessorObservers();
	}

}

package me.mil.lmc.frontend.gui.components;

import me.mil.lmc.backend.MemorySlot;
import me.mil.lmc.backend.Processor;
import me.mil.lmc.backend.ProcessorObserverNotification;
import me.mil.lmc.backend.ProcessorObserverNotificationType;
import me.mil.lmc.backend.util.Pair;
import me.mil.lmc.frontend.gui.AbstractGraphicalInterface;
import me.mil.lmc.frontend.gui.LMCProcessorObserver;
import me.mil.lmc.frontend.gui.util.GBCBuilder;
import me.mil.lmc.frontend.gui.util.InterfaceUtils;
import me.mil.lmc.frontend.gui.util.StyleConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MemoryViewPanel extends LMCSubPanel {

	private Map<Integer, MemoryUnit> memoryUnits = new HashMap<>();

	private Container memoryUnitContainer;

	private ExecutorService animationService;

	public MemoryViewPanel(AbstractGraphicalInterface lmcInterface) {
		super(lmcInterface);
	}

	@Override
	protected void addToRoot(RootPanel root) {
		root.add(this, new GBCBuilder().setAnchor(GBCBuilder.Anchor.LINE_START).setFill(GBCBuilder.Fill.BOTH)
				.setWeight(0.65, 0.96).
				setCellsConsumed(1, 2)
				.setPosition(1, 2).build());
	}

	@Override
	protected void generate() {
		this.animationService = Executors.newFixedThreadPool(100);

		setLayout(new BorderLayout());
		setBackground(StyleConstants.COLOR_MEMORY_VIEW_BACKGROUND);

		JPanel memoryUnitContainer = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 10));
		memoryUnitContainer.setBackground(getBackground());
		memoryUnitContainer.setBorder(new EmptyBorder(15, 10, 15, 10));

		JScrollPane scrollPane = new JScrollPane(memoryUnitContainer);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		this.memoryUnitContainer = memoryUnitContainer;

		add(scrollPane);

		new LMCProcessorObserver(getInterface()) {
			@Override
			public void onUpdate(Processor processor, ProcessorObserverNotification notification) {
				SwingUtilities.invokeLater(() -> {
					if (notification.getType() == ProcessorObserverNotificationType.SET_MEMORY) {
						Pair<Integer, MemorySlot> newVal = (Pair<Integer, MemorySlot>) notification.getNewValue();
						memoryUnits.get(newVal.getA()).setOpCode(newVal.getB().getValue());
					}
					if (notification.getType() == ProcessorObserverNotificationType.CLEAR_MEMORY) {
						memoryUnits.forEach((id, unit) -> unit.setOpCode(processor.getMemorySlotValue(id)));
						return;
					}

					if (notification.getType() == ProcessorObserverNotificationType.SET_MEMORY_SIZE) {
						resetMemoryUnits();
					}
				});
			}
		};

	}

	// clear, then instantiate
	public void resetMemoryUnits() {
		clearMemoryUnits();
		instantiateMemoryUnits();
		repaint();
	}

	public void instantiateMemoryUnits() {
		for (int i = 0; i < getInterface().getProcessor().getMemorySize(); i++) { // Instantiate Memory Units
			addMemoryUnit(i);
		}
	}

	public void addMemoryUnit(int id) {
		MemoryUnit memUnit = new MemoryUnit(getInterface(), id);
		getMemoryUnitContainer().add(memUnit);
		memoryUnits.put(id, memUnit);
		getMemoryUnitContainer().revalidate(); // refresh
	}

	public void removeMemoryUnit(int id) {
		getMemoryUnitContainer().remove(memoryUnits.get(id));
		memoryUnits.remove(id);
		getMemoryUnitContainer().revalidate();
	}

	public void clearMemoryUnits() {
		new HashSet<>(memoryUnits.keySet()).forEach(this::removeMemoryUnit);
	}

	Container getMemoryUnitContainer() {
		return memoryUnitContainer;
	}

	private ExecutorService getAnimationService() {
		return animationService;
	}

	private static class MemoryUnit extends LMCPanel {

		private final int id;
		private JLabel labelOpCode;

		public MemoryUnit(AbstractGraphicalInterface lmcInterface, int id) {
			super(lmcInterface, false);
			this.id = id;
			generate();
		}

		@Override
		protected void generate() {
			setLayout(new GridBagLayout());

			JPanel panelID = new JPanel(new GridBagLayout());
			panelID.setBorder(StyleConstants.BORDER_EMPTY);
			panelID.setBackground(StyleConstants.COLOR_MEMORY_UNIT_ID_BACKGROUND);
			panelID.setPreferredSize(new Dimension(50, 20));
			panelID.setSize(panelID.getPreferredSize());

			JLabel labelID = new JLabel(InterfaceUtils.padInteger(id));

			labelID.setForeground(StyleConstants.COLOR_MEMORY_UNIT_ID_FOREGROUND);
			panelID.add(labelID, new GridBagConstraints()); // Places label centred in both axes

			JPanel panelOpCode = new JPanel(new GridBagLayout()); // Unfortunately, you can't .clone() swing components, so some duplication here
			panelOpCode.setBorder(StyleConstants.BORDER_EMPTY);
			panelOpCode.setPreferredSize(panelID.getPreferredSize());
			panelOpCode.setSize(panelOpCode.getPreferredSize());

			JLabel labelOpCode = new JLabel("000", SwingConstants.CENTER);
			labelOpCode.setForeground(StyleConstants.COLOR_MEMORY_UNIT_OPCODE_FOREGROUND);
			labelOpCode.setPreferredSize(panelOpCode.getPreferredSize());
			panelOpCode.add(labelOpCode, new GridBagConstraints());

			GBCBuilder gbcBuilder = new GBCBuilder().setAnchor(GBCBuilder.Anchor.NORTH).setFill(GBCBuilder.Fill.BOTH)
					.setWeight(1, 0.5).setPositionX(0);
			add(panelID, gbcBuilder.setPositionY(0).build());
			add(panelOpCode, gbcBuilder.setPositionY(1).build());

			this.labelOpCode = labelOpCode;
		}

		public void setOpCode(int newOpCode) {
			String oldText = labelOpCode.getText();
			labelOpCode.setText(InterfaceUtils.padInteger(newOpCode));
			if (!Objects.equals(oldText, labelOpCode.getText())) {
				playUpdateAnimation(getInterface().getMemoryViewPanel().getAnimationService());
			}
			labelOpCode.paintImmediately(labelOpCode.getVisibleRect()); // refresh
		}

		private boolean animationPlaying = false;
		public void playUpdateAnimation(ExecutorService service) {
			if (animationPlaying) return;
			animationPlaying = true;

			Color initColor = StyleConstants.COLOR_MEMORY_UNIT_OPCODE_FOREGROUND;
			Color updateColor = StyleConstants.COLOR_MEMORY_UNIT_ID_FOREGROUND_UPDATED;

			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					for (int i = 0; i <= 100; i++) {
						labelOpCode.setForeground(mixColours(initColor, updateColor, (double) i / 100));
						Thread.sleep(3);
					}
					animationPlaying = false;
					return null;
				}
			};
			service.submit(worker);

		}

		private Color mixColours(Color color1, Color color2, double percent) {
			final double inversePercentage = 1.0 - percent;
			final int red = (int) (color1.getRed() * percent + color2.getRed() * inversePercentage);
			final int green = (int) (color1.getGreen() * percent + color2.getGreen() * inversePercentage);
			final int blue = (int) (color1.getBlue() * percent + color2.getBlue() * inversePercentage);
			return new Color(red, green, blue);
		}

	}
}

package com.apress.progwt.client.calculator;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.TextBox;

public class Calculator extends Composite {

	/**
	 * Format is postive;negative. We want to override the default thousands separator ',' to help
	 * ease our parser.
	 */
	private static final NumberFormat nf = NumberFormat.getDecimalFormat().getFormat(
			"###0.#####;-###0.#####");

	private ControlAction currenAction;
	private boolean doClearOnNextDigit;

	private TextBox inputBox;
	private double lastNum = 0;

	public Calculator() {

		DockPanel dockPanel = new DockPanel();

		Grid controls = new Grid(5, 2);
		Grid numbersP = new Grid(4, 3);

		// initialize the 1-9 buttons
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				numbersP.setWidget(row, col, new NumberButton(this, row * 3 + col + 1));
			}
		}
		numbersP.setWidget(3, 0, new NumberButton(this, 0));
		numbersP.setWidget(3, 1, new NumberButton(this, "."));
		numbersP.setWidget(3, 2, new ControlButton(this, new ControlAction(this, "+/-") {

			// @Override
			public boolean isMultiArg() {
				return false;
			}

			// @Override
			public double performAction(double previous, double current) {
				return -1 * current;
			}
		}));

		controls.setWidget(0, 0, new ControlButton(this, new ControlAction(this, "/") {

			// @Override
			public double performAction(double previous, double current) {
				return previous / current;
			}
		}));

		controls.setWidget(0, 1, new ControlButton(this, new ControlAction(this, "sqrt") {
			// @Override
			public boolean isMultiArg() {
				return false;
			}

			// @Override
			public double performAction(double previous, double current) {
				return Math.sqrt(current);
			}
		}));


		controls.setWidget(1, 0, new ControlButton(this, new ControlAction(this, "*") {
			// @Override
			public double performAction(double previous, double current) {
				return previous * current;
			}
		}));
		controls.setWidget(1, 1, new ControlButton(this, new ControlAction(this, "%") {
			// @Override
			public boolean isMultiArg() {
				return false;
			}

			// @Override
			public double performAction(double previous, double current) {
				return previous % current;
			}
		}));
		controls.setWidget(2, 0, new ControlButton(this, new ControlAction(this, "-") {
			// @Override
			public double performAction(double previous, double current) {
				return previous - current;
			}
		}));
		controls.setWidget(2, 1, new ControlButton(this, new ControlAction(this, "1/x") {
			// @Override
			public boolean isMultiArg() {
				return false;
			}

			// @Override
			public double performAction(double previous, double current) {
				return 1 / current;
			}
		}));
		controls.setWidget(3, 0, new ControlButton(this, new ControlAction(this, "+") {
			// @Override
			public double performAction(double previous, double current) {
				return previous + current;
			}
		}));
		controls.setWidget(3, 1, new ControlButton(this, new ControlAction(this, "=") {
			public boolean isMultiArg() {
				return false;
			}

			// @Override
			public double performAction(double previous, double current) {
				return current;
			}

			// @Override
			public boolean useSelf() {
				return false;
			}
		}));

		controls.setWidget(4, 0, new ControlButton(this, new ControlAction(this, "bksp") {
			public boolean isMultiArg() {
				return false;
			}

			// @Override
			public double performAction(double previous, double current) {
				String cStr = current + "";
				if (cStr.endsWith(".0")) {
					cStr = cStr.substring(0, cStr.length() - 3);
				} else {
					cStr = cStr.substring(0, cStr.length() - 1);
				}
				if (cStr.equals("")) {
					cStr = "0";
				}
				return Double.parseDouble(cStr);
			}

		}));

		controls.setWidget(4, 1, new ControlButton(this, new ControlAction(this, "clear") {
			public boolean isMultiArg() {
				return false;
			}


			// @Override
			public double performAction(double previous, double current) {
				return 0;
			}
		}));



		dockPanel.add(numbersP, DockPanel.CENTER);
		dockPanel.add(controls, DockPanel.EAST);

		inputBox = new TextBox();
		inputBox.addStyleName("ResultBox");
		dockPanel.add(inputBox, DockPanel.NORTH);

		initWidget(dockPanel);


		setResult(0);

	}

	public void actionClick(ControlAction action) {
		if (action.isMultiArg()) {
			lastNum = getCurrentNum();
			setDoClearOnNextDigit(true);
			this.currenAction = action;

		} else {
			if (action.useSelf()) {
				setResult(action.performAction(getLastNum(), getCurrentNum()));
			} else {
				setResult(currenAction.performAction(getLastNum(), getCurrentNum()));
			}
		}
	}

	public void digitAction(String value) {
		if (isDoClearOnNextDigit()) {
			inputBox.setText("");
			setDoClearOnNextDigit(false);
		}
		inputBox.setText(inputBox.getText() + value);
	}

	private double getCurrentNum() {
		return Double.parseDouble(inputBox.getText());
	}

	private double getLastNum() {
		return lastNum;
	}

	public boolean isDoClearOnNextDigit() {
		return doClearOnNextDigit;
	}

	public void setDoClearOnNextDigit(boolean doClearOnNextDigit) {
		this.doClearOnNextDigit = doClearOnNextDigit;
	}

	private void setResult(double res) {
		inputBox.setText(nf.format(res));
		if (res == 0) {
			setDoClearOnNextDigit(true);
		}
	}


}

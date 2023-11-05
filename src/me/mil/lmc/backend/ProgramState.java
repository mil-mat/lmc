package me.mil.lmc.backend;

public class ProgramState {

	private Program programRef;

	private Register[] registers;
	private int[] ram;
	private boolean halting;

	public ProgramState(Program program, Register[] registers, int[] ram, boolean halt) {
		this.programRef = program;
		this.registers = registers;
		this.ram = ram;
		this.halting = halt;
	}

	public Program getProgram() {
		return programRef;
	}

	public int getValue(int ramLocation) {
		return ram[ramLocation];
	}

	public ProgramState setValue(int ramLocation, int newValue) {
		ram[ramLocation] = newValue;
		return this;
	}

	public ProgramState setRegister(RegisterType register, int newValue) {
		registers[register.ordinal()].setValue(newValue);
		return this;
	}

	public Register getRegister(RegisterType registerType) {
		return registers[registerType.ordinal()];
	}

	public ProgramState copyFromRegister(RegisterType from, RegisterType to) {
		setRegister(to, getRegister(from).getValue());
		return this;
	}

	public Register[] getRegisters() {
		return registers;
	}

	public void setRegisters(Register[] registers) {
		this.registers = registers;
	}

	public int[] getRam() {
		return ram;
	}

	public ProgramState setRam(int[] ram) {
		this.ram = ram;
		return this;
	}

	public boolean isHalting() {
		return halting;
	}

	public ProgramState setHalting(boolean halt) {
		this.halting = halt;
		return this;
	}
}

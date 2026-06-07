package pcd.tasks.controller;
import pcd.tasks.model.Board;

public interface Cmd {
    void execute(Board board);
}
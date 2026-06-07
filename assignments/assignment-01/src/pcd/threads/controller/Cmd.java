package pcd.threads.controller;
import pcd.threads.model.Board;

public interface Cmd {
    void execute(Board board);
}
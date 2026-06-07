package pcd.sequential.controller;
import pcd.sequential.model.*;

public interface Cmd {
    void execute(Board board);
}
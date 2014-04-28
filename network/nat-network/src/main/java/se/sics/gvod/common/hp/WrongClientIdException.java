/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.common.hp;

/**
 *
 * @author Owner
 */
public class WrongClientIdException extends RuntimeException
{

    public WrongClientIdException(String msg) {
        super(msg);
    }

}

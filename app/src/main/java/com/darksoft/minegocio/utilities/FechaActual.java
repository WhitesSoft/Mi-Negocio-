package com.darksoft.minegocio.utilities;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FechaActual {

    //Fecha actual
    Calendar c = Calendar.getInstance();
    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    String fechaactual = df.format(c.getTime());

    public String fechaActual(){
        return fechaactual;
    }

}

package com.kolaps;


import fr.lip6.move.pnml.framework.general.PnmlExport;
import fr.lip6.move.pnml.framework.utils.ModelRepository;
import fr.lip6.move.pnml.framework.utils.exception.*;
import fr.lip6.move.pnml.hlpn.hlcorestructure.PetriNet;
import fr.lip6.move.pnml.hlpn.hlcorestructure.PetriNetDoc;
import fr.lip6.move.pnml.hlpn.hlcorestructure.hlapi.RefPlaceHLAPI;
import fr.lip6.move.pnml.pnmlcoremodel.hlapi.*;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String jarFilePath = null;

        // Парсим аргументы
        for (int i = 0; i < args.length; i++) {
            if ("-f".equals(args[i]) && i + 1 < args.length) {
                jarFilePath = args[i + 1];
                break;
            }
        }

        // Проверяем, передан ли файл
        if (jarFilePath == null) {
            System.out.println("Ошибка: укажите путь к JAR-файлу с ключом -f");
            System.out.println("Пример: java -jar stal.jar -f path/to/file.jar");
            System.exit(1);
        }
        BytecodeParser parser = new BytecodeParser(jarFilePath);

    }
}
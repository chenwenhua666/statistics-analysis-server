package com.plm.common.exception.file;

import com.plm.common.exception.BaseException;

/**
 * 文件信息异常类
 *
 * @author cwh
 */
public class FileException extends BaseException {
    private static final long serialVersionUID = 1L;

    public FileException(String code, Object[] args) {
        super("file", code, args, null);
    }

}

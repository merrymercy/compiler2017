;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;; lib begin ;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

__int_format:
    db "%d", 0

;========== IO ==========
print:
    xor rax, rax
    call printf
    ret

println:
    call puts
    ret

printInt_:
    sub rsp, 8
    mov rsi, rdi
    mov rdi, __int_format
    xor rax, rax
    call printf
    add rsp, 8
    ret

getInt:
    sub rsp, 8

    mov rsi, rsp
    mov rdi, __int_format
    xor rax, rax
    call scanf
    mov rax, [rsp]

    add rsp, 8
    ret

toString:
    push rbx
    sub rsp, 128

    mov rdx, rdi
    mov rdi, rsp
    mov rsi, __int_format
    xor rax, rax
    call sprintf

    mov rdi, rsp
    call strlen

    mov rbx, rax ; length
    lea rdi, [rax + 5]
    call malloc
    mov dword [rax], ebx; store len

    lea rdi, [rax + 4]; 
    mov rsi, rsp
    call strcpy

    add rsp, 128
    pop rbx
    ret

getString:
    push rbx
    sub rsp, 128

    mov rdi, rsp
    call gets

    mov rdi, rsp
    call strlen

    mov rbx, rax ; length
    lea rdi, [rax + 5]
    call malloc

    mov dword [rax], ebx ; store length

    lea rdi, [rax + 4]
    mov rsi, rsp
    call strcpy

    ;mov byte [rax + rbx + 1], 0

    add rsp, 128
    pop rbx
    ret


__lib_array_size:
    mov eax, dword [rdi -4]
    ret

__lib_str_operator_ADD:
    push rbx
    push r12
    push r13

    mov r12, rdi ; save parameters
    mov r13, rsi

    mov ebx, dword [r12 - 4]
    add ebx, dword [r13 - 4] ; store length

    lea rdi, [rbx + 5]
    call malloc

    mov dword [rax], ebx
    lea rdi, [rax + 4]
    mov rsi, r12
    call strcpy

    mov rbx, rax ; return value

    mov edi, dword [r12 - 4]
    add rdi, rbx
    mov rsi, r13
    call strcpy

    mov rax, rbx

    pop r13
    pop r12
    pop rbx
    ret

__lib_str_length:
    mov eax, dword [rdi -4]
    ret

__lib_str_substring:
    push rbx
    push r12
    
    lea r12, [rdi + rsi]
    neg rsi
    lea rbx, [rdx + rsi + 1]


    lea rdi, [rbx + 5]
    call malloc

    mov dword[rax], ebx
    lea rdi, [rax + 4]
    lea rsi, [r12]
    mov rdx, rbx
    call strncpy

    pop r12
    pop rbx
    ret

__lib_str_parseInt:
    sub rsp, 8

    mov rsi, __int_format
    mov rdx, rsp
    xor rax, rax
    call sscanf

    mov eax, dword [rsp]
    add rsp, 8
    ret

__lib_str_ord:
    mov al, byte [rdi + rsi]
    ret


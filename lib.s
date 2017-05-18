;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;; lib begin ;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

__int_format:
    db "%ld", 0

__intln_format:
    db "%ld", 10, 0

;========== IO ==========
print:
    sub rsp, 8

    xor rax, rax
    call printf

    add rsp, 8
    ret

println:
    sub rsp, 8

    call puts

    add rsp, 8
    ret

__lib_printInt:
    sub rsp, 8

    mov rsi, rdi
    mov rdi, __int_format
    xor rax, rax
    call printf

    add rsp, 8
    ret

__lib_printlnInt:
    sub rsp, 8

    mov rsi, rdi
    mov rdi, __intln_format
    xor rax, rax
    call printf

    add rsp, 8
    ret

getInt:
    sub rsp, 8

    mov rsi, rsp
    mov rdi, __int_format
    xor eax, eax
    call scanf
    call getchar
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

    add rsp, 128
    pop rbx
    ret

__lib_array_size:
    sub rsp, 8

    mov eax, dword [rdi -4]

    add rsp, 8
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
    sub rsp, 8

    mov eax, dword [rdi -4]

    add rsp, 8
    ret

__lib_str_substring:
    push rbx
    push r12
    sub rsp, 8
    
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

    add rsp, 8
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
    sub rsp, 8
    xor rax, rax
    mov al, byte [rdi + rsi]

    add rsp, 8
    ret

__lib_str_operator_LT:
    sub rsp, 8

    call strcmp
    cmp rax, 0
    setl al
    movzx rax, al

    add rsp, 8
    ret

__lib_str_operator_EQ:
    sub rsp, 8

    call strcmp
    cmp rax, 0
    sete al
    movzx rax, al

    add rsp, 8
    ret

__lib_str_operator_GT:
    sub rsp, 8

    call strcmp
    cmp rax, 0
    setg al
    movzx rax, al

    add rsp, 8
    ret


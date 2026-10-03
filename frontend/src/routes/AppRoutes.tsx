import { Navigate, Route, Routes } from 'react-router-dom'
import {DepartmentsListPage} from "../features/departments/pages/DepartmentListPage.tsx";
import {DepartmentCreatePage} from "../features/departments/pages/DepartmentCreatePage.tsx";
import {DepartmentDetailPage} from "../features/departments/pages/DepartmentDetailPage.tsx";
import {DepartmentEditPage} from "../features/departments/pages/DepartmentEditPage.tsx";
import {AppLayout} from "../features/departments/components/layout/AppLayout.tsx";

export function AppRoutes() {
    return (
        <Routes>
            <Route element={<AppLayout />}>
            <Route
                path="/"
                element={<Navigate to="/departamentos" replace />}
            />

            <Route
                path="/departamentos"
                element={<DepartmentsListPage />}
            />

            <Route
                path="/departamentos/nuevo"
                element={<DepartmentCreatePage />}
            />

            <Route
                path="/departamentos/:id"
                element={<DepartmentDetailPage />}
            />

            <Route
                path="/departamentos/:id/editar"
                element={<DepartmentEditPage />}
            />
            </Route>
        </Routes>
    )
}